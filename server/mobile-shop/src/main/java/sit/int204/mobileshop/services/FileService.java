package sit.int204.mobileshop.services;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import sit.int204.mobileshop.config.FileStorageProperties;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.entities.SaleItemImage;
import sit.int204.mobileshop.exceptions.FileStorageException;
import sit.int204.mobileshop.repositories.SaleItemImageRepository;
import sit.int204.mobileshop.repositories.SaleItemRepository;
import sit.int204.mobileshop.dtos.SaleItemImageRequest;

@Service
public class FileService {
    private static final Logger log = Logger.getLogger(FileService.class.getName());
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");
    private static final List<String> ALLOWED_CONTENT_TYPES = List.of("image/jpeg", "image/jpg", "image/png", "image/webp");

    private final Path baseStoragePath;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private SaleItemImageRepository saleItemImageRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;

    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Value("${app.file.upload-dir:uploads}")
    private String uploadDir;

    @Value("${app.file.max-size:5242880}") // 5MB default
    private long maxFileSize;

    @Value("${app.base-url:http://localhost:8080/api/v1/images/}")
    private String baseUrl;

    /**
     * Constructor to initialize file storage path.
     */
    public FileService(FileStorageProperties fileStorageProperties) {
        this.baseStoragePath = Paths.get(fileStorageProperties.getUploadDir()).toAbsolutePath().normalize();
        try {
            if (!Files.exists(this.baseStoragePath)) {
                Files.createDirectories(this.baseStoragePath);
                log.info("Created storage directory: " + this.baseStoragePath);
            }
            if (!Files.isWritable(this.baseStoragePath)) {
                throw new FileStorageException("Storage directory is not writable: " + this.baseStoragePath);
            }
        } catch (IOException ex) {
            throw new FileStorageException("Could not create or access base directory for file storage: " + this.baseStoragePath, ex);
        }
    }

    /**
     * Save single file - legacy method สำหรับ backward compatibility
     * @param file MultipartFile to save
     * @param saleItemId sale item ID
     * @param imageViewOrder order of the image
     * @return saved SaleItemImage entity
     */
    @Transactional
    public SaleItemImage saveFile(MultipartFile file, Integer saleItemId, Integer imageViewOrder) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File cannot be empty");
        }

        if (saleItemId == null) {
            throw new FileStorageException("SaleItem ID cannot be null");
        }

        validateFile(file);

        try {
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String safeFileName = sanitizeFileName(originalFileName);
            String uniqueFileName = generateUniqueFileName(safeFileName, saleItemId);

            Path targetFile = baseStoragePath.resolve(uniqueFileName);

            // Copy file to storage
            copyFileToStorage(file, targetFile, originalFileName);

            // Create SaleItemImage entity
            SaleItem saleItem = saleItemRepository.findById(saleItemId)
                    .orElseThrow(() -> new FileStorageException("SaleItem not found with ID: " + saleItemId));

            SaleItemImage image = new SaleItemImage();
            image.setSaleItem(saleItem);
            image.setFileName(uniqueFileName);
            image.setImageViewOrder(imageViewOrder);
            image.setCreatedOn(Instant.now());
            image.setUpdatedOn(Instant.now());

            // Save to database
            return saleItemImageRepository.save(image);

        } catch (IOException e) {
            throw new FileStorageException("Could not store file " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Enhanced method to save images with proper ordering and metadata management
     */
    @Transactional
    public List<SaleItemImage> saveImagesWithOrder(List<SaleItemImageRequest> imageRequests, Integer saleItemId) {
        if (saleItemId == null) {
            throw new FileStorageException("SaleItem ID cannot be null");
        }

        // Synchronize on saleItemId to prevent concurrent modifications
        synchronized (saleItemId.toString().intern()) {
            // Get current images
            Map<String, SaleItemImage> currentImagesMap = saleItemImageRepository.findAllBySaleItemId(saleItemId)
                    .stream()
                    .collect(Collectors.toMap(SaleItemImage::getFileName, img -> img));

            List<SaleItemImage> finalImages = new ArrayList<>();
            List<String> filesToDelete = new ArrayList<>();

            // Process each image request
            for (SaleItemImageRequest request : Optional.ofNullable(imageRequests).orElse(Collections.emptyList())) {
                if (request == null) continue;

                String status = Optional.ofNullable(request.getStatus()).orElse("").toUpperCase();

                switch (status) {
                    case "DELETE":
                        if (request.getFileName() != null) {
                            filesToDelete.add(request.getFileName());
                        }
                        break;

                    case "NEW":
                        if (request.getImageFile() != null && !request.getImageFile().isEmpty()) {
                            SaleItemImage newImage = processNewImage(request, saleItemId);
                            finalImages.add(newImage);
                        }
                        break;

                    case "EXISTING":
                    case "MOVE":
                        SaleItemImage existingImage = currentImagesMap.get(request.getFileName());
                        if (existingImage != null) {
                            existingImage.setImageViewOrder(request.getOrder());
                            existingImage.setUpdatedOn(Instant.now());
                            finalImages.add(existingImage);
                        }
                        break;
                }
            }

            // Delete specified files
            if (!filesToDelete.isEmpty()) {
                deleteSpecificImages(saleItemId, filesToDelete);
            }

            // Sort and reorder images
            finalImages.sort(Comparator.comparingInt(SaleItemImage::getImageViewOrder));

            // Reassign sequential order numbers
            int order = 1;
            for (SaleItemImage image : finalImages) {
                image.setImageViewOrder(order++);
            }

            // Save all images to database
            List<SaleItemImage> savedImages = saleItemImageRepository.saveAll(finalImages);

            // Save metadata for backup/reference
            saveImagesMetadata(saleItemId, savedImages);

            return savedImages;
        }
    }

    /**
     * Process new image upload
     */
    private SaleItemImage processNewImage(SaleItemImageRequest request, Integer saleItemId) {
        MultipartFile file = request.getImageFile();
        validateFile(file);

        try {
            String originalFileName = StringUtils.cleanPath(file.getOriginalFilename());
            String safeFileName = sanitizeFileName(originalFileName);
            String uniqueFileName = generateUniqueFileName(safeFileName, saleItemId);

            Path targetFile = baseStoragePath.resolve(uniqueFileName);

            // Copy file to storage
            copyFileToStorage(file, targetFile, originalFileName);

            // Create SaleItemImage entity
            SaleItem saleItem = saleItemRepository.findById(saleItemId)
                    .orElseThrow(() -> new FileStorageException("SaleItem not found with ID: " + saleItemId));

            SaleItemImage image = new SaleItemImage();
            image.setSaleItem(saleItem);
            image.setFileName(uniqueFileName);
            image.setImageViewOrder(request.getOrder());
            image.setCreatedOn(Instant.now());
            image.setUpdatedOn(Instant.now());

            return image;

        } catch (IOException e) {
            throw new FileStorageException("Could not store file " + file.getOriginalFilename(), e);
        }
    }

    /**
     * Generate unique filename to avoid conflicts
     * Now includes saleItemId in the filename for uniqueness
     */
    public String generateUniqueFileName(String originalName, Integer saleItemId) {
        String name = originalName;
        String extension = "";
        int dotIndex = originalName.lastIndexOf('.');
        if (dotIndex > 0) {
            name = originalName.substring(0, dotIndex);
            extension = originalName.substring(dotIndex);
        }

        // Include saleItemId and timestamp for uniqueness
        String timestamp = String.valueOf(System.currentTimeMillis());
        String baseFileName = saleItemId + "_" + timestamp + "_" + name;

        int counter = 1;
        String uniqueName;
        Path path;
        do {
            uniqueName = baseFileName + (counter == 1 ? "" : "_" + counter) + extension;
            path = baseStoragePath.resolve(uniqueName);
            counter++;
        } while (Files.exists(path));

        return uniqueName;
    }

    /**
     * Sanitize filename to prevent security issues
     */
    private String sanitizeFileName(String fileName) {
        if (fileName == null) return "unnamed";
        return fileName.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
    }

    /**
     * Delete specific images by filename
     */
    public void deleteSpecificImages(Integer saleItemId, List<String> fileNames) {
        if (saleItemId == null || fileNames == null || fileNames.isEmpty()) {
            return;
        }

        // Delete physical files
        fileNames.forEach(fileName -> {
            try {
                Path filePath = baseStoragePath.resolve(fileName);
                Files.deleteIfExists(filePath);
                log.info("Deleted file: " + fileName);
            } catch (IOException e) {
                log.warning("Failed to delete file: " + fileName + " - " + e.getMessage());
            }
        });

        // Delete from database
        List<SaleItemImage> imagesToDelete = saleItemImageRepository.findAllBySaleItemId(saleItemId)
                .stream()
                .filter(img -> fileNames.contains(img.getFileName()))
                .collect(Collectors.toList());

        saleItemImageRepository.deleteAll(imagesToDelete);
    }

    /**
     * Delete all images for a sale item
     */
    public void deleteImagesForSaleItem(Integer saleItemId) {
        if (saleItemId == null) return;

        // Get all images for this sale item
        List<SaleItemImage> images = saleItemImageRepository.findAllBySaleItemId(saleItemId);

        // Delete physical files
        images.forEach(image -> {
            try {
                Path filePath = baseStoragePath.resolve(image.getFileName());
                Files.deleteIfExists(filePath);
                log.info("Deleted file: " + image.getFileName());
            } catch (IOException e) {
                log.warning("Failed to delete file: " + image.getFileName() + " - " + e.getMessage());
            }
        });

        // Delete from database
        saleItemImageRepository.deleteAll(images);

        // Delete metadata file
        deleteMetadataFile(saleItemId);
    }

    /**
     * Save images metadata as JSON for backup/reference
     */
    private void saveImagesMetadata(Integer saleItemId, List<SaleItemImage> images) {
        try {
            Path metadataFile = getMetadataFilePath(saleItemId);
            Files.createDirectories(metadataFile.getParent());

            List<ImageMetadata> metadata = images.stream()
                    .map(img -> new ImageMetadata(
                            img.getFileName(),
                            img.getImageViewOrder(),
                            img.getCreatedOn(),
                            img.getUpdatedOn()
                    ))
                    .collect(Collectors.toList());

            objectMapper.writeValue(metadataFile.toFile(), metadata);
            log.info("Saved metadata for saleItem: " + saleItemId);
        } catch (Exception e) {
            log.warning("Failed to save metadata for saleItem: " + saleItemId + " - " + e.getMessage());
        }
    }

    /**
     * Get metadata file path - now stored in base directory with saleItemId prefix
     */
    private Path getMetadataFilePath(Integer saleItemId) {
        return baseStoragePath.resolve(saleItemId + "_images_metadata.json");
    }

    /**
     * Delete metadata file
     */
    private void deleteMetadataFile(Integer saleItemId) {
        try {
            Path metadataFile = getMetadataFilePath(saleItemId);
            Files.deleteIfExists(metadataFile);
            log.info("Deleted metadata file for saleItem: " + saleItemId);
        } catch (IOException e) {
            log.warning("Failed to delete metadata file for saleItem: " + saleItemId);
        }
    }

    /**
     * Load images metadata from JSON
     */
    public List<ImageMetadata> loadImagesMetadata(Integer saleItemId) {
        Path metadataFile = getMetadataFilePath(saleItemId);
        if (!Files.exists(metadataFile)) {
            return new ArrayList<>();
        }

        try {
            List<ImageMetadata> metadata = objectMapper.readValue(
                    metadataFile.toFile(),
                    new TypeReference<List<ImageMetadata>>() {}
            );

            metadata.sort(Comparator.comparingInt(ImageMetadata::getOrder));
            return metadata;
        } catch (Exception e) {
            log.warning("Failed to load metadata for saleItem: " + saleItemId);
            return new ArrayList<>();
        }
    }

    // === Enhanced validation methods ===

    /**
     * Enhanced file validation
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File must not be empty");
        }

        if (file.getSize() > maxFileSize) {
            throw new FileStorageException(String.format("File size (%d bytes) exceeds limit of %d bytes",
                    file.getSize(), maxFileSize));
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new FileStorageException("Invalid file type: " + contentType + ". Allowed types: " + ALLOWED_CONTENT_TYPES);
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileStorageException("Invalid file extension: ." + extension + ". Allowed extensions: " + ALLOWED_EXTENSIONS);
        }
    }

    /**
     * Validate multiple files at once
     */
    public void validateMultipleFiles(List<MultipartFile> files) {
        if (files == null || files.isEmpty()) {
            return;
        }

        List<String> validationErrors = new ArrayList<>();
        long totalSize = 0;

        for (MultipartFile file : files) {
            if (file == null || file.isEmpty()) {
                validationErrors.add("Empty file detected");
                continue;
            }

            totalSize += file.getSize();

            try {
                validateFile(file);
            } catch (FileStorageException e) {
                validationErrors.add(e.getMessage());
            }
        }

        // Check total upload size (10x single file limit)
        if (totalSize > maxFileSize * 10) {
            validationErrors.add("Total upload size (" + totalSize + " bytes) exceeds limit of " + (maxFileSize * 10) + " bytes");
        }

        if (!validationErrors.isEmpty()) {
            throw new FileStorageException("Validation errors: " + String.join("; ", validationErrors));
        }
    }

    // === Utility methods ===

    /**
     * Check if file exists in storage
     */
    public boolean fileExists(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            return false;
        }

        try {
            Path filePath = baseStoragePath.resolve(filename).normalize();

            // Security check - prevent directory traversal
            if (!filePath.startsWith(baseStoragePath)) {
                log.warning("Invalid file path attempted: " + filename);
                return false;
            }

            return Files.exists(filePath) && Files.isRegularFile(filePath);
        } catch (Exception e) {
            log.warning("Error checking file existence: " + filename + " - " + e.getMessage());
            return false;
        }
    }

    /**
     * Check multiple files existence
     */
    public Map<String, Boolean> checkMultipleFilesExist(List<String> filenames) {
        return filenames.parallelStream()
                .collect(Collectors.toMap(
                        filename -> filename,
                        this::fileExists
                ));
    }

    /**
     * Load file as Resource
     */
    public Resource loadFileAsResource(String filename) {
        try {
            Path filePath = baseStoragePath.resolve(filename).normalize();

            if (!filePath.startsWith(baseStoragePath)) {
                throw new FileStorageException("Invalid file path: " + filename);
            }

            Resource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            } else {
                throw new FileStorageException("File not found or not readable: " + filename);
            }
        } catch (MalformedURLException ex) {
            throw new FileStorageException("File not found: " + filename, ex);
        }
    }

    /**
     * Async file deletion for large operations
     */
    @Async("fileTaskExecutor")
    public CompletableFuture<Void> deleteFilesAsync(Integer saleItemId, List<String> filenames) {
        return CompletableFuture.runAsync(() -> deleteSpecificImages(saleItemId, filenames));
    }

    // === Legacy compatibility methods (kept for backward compatibility) ===

    @Deprecated
    public String storeFile(MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            return null;
        }
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);
        String uniqueFilename = UUID.randomUUID().toString() + "." + fileExtension;

        Path filePath = baseStoragePath.resolve(uniqueFilename);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFilename;
    }

    // === Private helper methods ===

    private String getFileExtension(String filename) {
        if (filename == null || filename.isEmpty()) {
            throw new FileStorageException("Filename cannot be null or empty");
        }

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new FileStorageException("File does not have an extension: " + filename);
        }
        return filename.substring(dotIndex + 1);
    }

    private void copyFileToStorage(MultipartFile file, Path targetFile, String originalFilename) throws IOException {
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Successfully copied file: " + originalFilename + " to " + targetFile);
        }
    }

    /**
     * Generate file URL for frontend access
     * @param fileName the filename
     * @return complete URL to access the file
     */
    public String generateFileUrl(String fileName) {
        return baseUrl + fileName;
    }

    /**
     * Get all image URLs for a sale item
     * @param saleItemId the sale item ID
     * @return list of image URLs sorted by order
     */
    public List<String> getImageUrls(Integer saleItemId) {
        return saleItemImageRepository.findAllBySaleItemId(saleItemId)
                .stream()
                .sorted(Comparator.comparingInt(SaleItemImage::getImageViewOrder))
                .map(img -> generateFileUrl(img.getFileName()))
                .collect(Collectors.toList());
    }

    /**
     * Image metadata for JSON serialization
     */
    public static class ImageMetadata {
        private String fileName;
        private Integer order;
        private Instant createdOn;
        private Instant updatedOn;

        public ImageMetadata() {}

        public ImageMetadata(String fileName, Integer order,
                             Instant createdOn, Instant updatedOn) {
            this.fileName = fileName;
            this.order = order;
            this.createdOn = createdOn;
            this.updatedOn = updatedOn;
        }

        // Getters and setters
        public String getFileName() { return fileName; }
        public void setFileName(String fileName) { this.fileName = fileName; }

        public Integer getOrder() { return order; }
        public void setOrder(Integer order) { this.order = order; }

        public Instant getCreatedOn() { return createdOn; }
        public void setCreatedOn(Instant createdOn) { this.createdOn = createdOn; }

        public Instant getUpdatedOn() { return updatedOn; }
        public void setUpdatedOn(Instant updatedOn) { this.updatedOn = updatedOn; }
    }
}