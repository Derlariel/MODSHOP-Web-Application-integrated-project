package sit.int204.mobileshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import sit.int204.mobileshop.config.FileStorageProperties;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.entities.SaleItemImage;
import sit.int204.mobileshop.exceptions.FileStorageException;
import sit.int204.mobileshop.repositories.SaleItemImageRepository;
import sit.int204.mobileshop.repositories.SaleItemRepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

/**
 * Service for handling file uploads, deletions, and retrievals.
 */
@Service
public class FileService {
    private static final Logger log = Logger.getLogger(FileService.class.getName());
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = List.of("jpg", "jpeg", "png", "webp");
    private final Path baseStoragePath;

    @Autowired
    private SaleItemImageRepository saleItemImageRepository;

    @Autowired
    private SaleItemRepository saleItemRepository;
    @Autowired
    private FileStorageProperties fileStorageProperties;

    /**
     * Constructor to initialize file storage path.
     *
     * @param fileStorageProperties the file storage configuration
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
     * Saves an uploaded image file and associates it with a SaleItem.
     *
     * @param file       the uploaded image file
     * @param saleItemId the ID of the SaleItem
     * @param isPrimary  whether the image is primary
     * @return the saved SaleItemImage entity
     * @throws FileStorageException if file validation or storage fails
     */
    @Transactional
    public SaleItemImage saveFile(MultipartFile file, Integer saleItemId, boolean isPrimary) {
        validateFile(file);
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = generateUniqueFilename(originalFilename);
        Path targetFile = baseStoragePath.resolve(uniqueFilename);

        log.info(String.format("Uploading file: %s for SaleItem ID: %d", originalFilename, saleItemId));

        try {
            copyFileToStorage(file, targetFile, originalFilename);

            SaleItem saleItem = saleItemRepository.findById(saleItemId)
                    .orElseThrow(() -> new FileStorageException("SaleItem not found with ID: " + saleItemId));

            if (isPrimary) {
                updateNonPrimaryImages(saleItemId);
            }

            SaleItemImage image = createSaleItemImage(saleItem, uniqueFilename, isPrimary);
            return saleItemImageRepository.save(image);

        } catch (IOException e) {
            cleanupFile(targetFile, uniqueFilename);
            throw new FileStorageException("Could not store file " + originalFilename + " at " + targetFile, e);
        }
    }

    /**
     * Deletes an image by its ID and removes the file from storage.
     *
     * @param id the ID of the image to delete
     * @throws FileStorageException if the image is not found or deletion fails
     */
    public void deleteImageById(Integer id) {
        SaleItemImage image = saleItemImageRepository.findById(id)
                .orElseThrow(() -> new FileStorageException("Image not found with ID: " + id));

        Path filePath = baseStoragePath.resolve(image.getImageUrl()).normalize();
        try {
            Files.deleteIfExists(filePath);
            saleItemImageRepository.delete(image);
            log.info("Deleted image with ID: " + id);
        } catch (IOException e) {
            throw new FileStorageException("Could not delete file: " + image.getImageUrl(), e);
        }
    }

    /**
     * Loads a file as a Resource.
     *
     * @param filename the name of the file to load
     * @return the file as a Resource
     * @throws FileStorageException if the file is not found or not readable
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

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File must not be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("File size exceeds limit of 5MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        validateFileName(originalFilename);

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileStorageException("Invalid file type: ." + extension + " is not supported.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("Only image files are allowed.");
        }
    }

    private void validateFileName(String filename) {
        if (filename == null || filename.isBlank() ||
                filename.contains("..") ||
                filename.contains("/") ||
                filename.contains("\\") ||
                !filename.matches("^[a-zA-Z0-9._-]+$")) {
            throw new FileStorageException("Invalid file name: " + filename);
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new FileStorageException("File does not have an extension: " + filename);
        }
        return filename.substring(dotIndex + 1);
    }

    private String generateUniqueFilename(String originalFilename) {
        String ext = getFileExtension(originalFilename);
        return UUID.randomUUID() + "." + ext;
    }

    private void copyFileToStorage(MultipartFile file, Path targetFile, String originalFilename) throws IOException {
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        log.info("Successfully copied file: " + originalFilename + " to " + targetFile);
    }

    private void updateNonPrimaryImages(Integer saleItemId) {
        saleItemImageRepository.updateAllNonPrimaryBySaleItemId(saleItemId);
    }

    private SaleItemImage createSaleItemImage(SaleItem saleItem, String uniqueFilename, boolean isPrimary) {
        SaleItemImage image = new SaleItemImage();
        image.setSaleItem(saleItem);
        image.setImageUrl(fileStorageProperties.getUploadDir() + '/' + uniqueFilename);
        image.setIsPrimary((byte) (isPrimary ? 1 : 0));
        image.setCreatedOn(Instant.now());
        image.setUpdatedOn(Instant.now());
        return image;
    }

    private void cleanupFile(Path targetFile, String filename) {
        try {
            Files.deleteIfExists(targetFile);
            log.warning("Cleaned up file after failed upload: " + filename);
        } catch (IOException e) {
            log.severe("Failed to clean up file: " + filename);
        }
    }
}