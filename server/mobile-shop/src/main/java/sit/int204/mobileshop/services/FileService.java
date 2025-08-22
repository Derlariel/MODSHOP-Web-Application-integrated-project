package sit.int204.mobileshop.services;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.scheduling.annotation.Async;
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
     * @param originalFileName       the uploaded image file
     * @param saleItemId the ID of the SaleItem
     * @param order  whether the image is primary
     * @return the saved SaleItemImage entity
     * @throws FileStorageException if file validation or storage fails
     */

    public String formatFileName(Integer saleItemId, Integer order, String originalFileName) {
        System.out.println("getFileExtension(originalFileName)" +getFileExtension(originalFileName));
        return  saleItemId + "." + order + "." + getFileExtension(originalFileName);
    }
    @Transactional
    public String updateFile(MultipartFile file, Integer saleItemId, Integer order) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = formatFileName(saleItemId, order, originalFilename);
        Path targetFile = baseStoragePath.resolve(uniqueFilename);
        try {
            copyFileToStorage(file, targetFile, originalFilename);

            return uniqueFilename;

        } catch (IOException e) {
            cleanupFile(targetFile, uniqueFilename);
            throw new FileStorageException("Could not store file " + originalFilename + " at " + targetFile, e);
        }
    }
    @Transactional
    public SaleItemImage saveFile(MultipartFile file, Integer saleItemId, Integer order) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = formatFileName(saleItemId, order, originalFilename);
        Path targetFile = baseStoragePath.resolve(uniqueFilename);

        log.info(String.format("Uploading file: %s for SaleItem ID: %d", originalFilename, saleItemId));

        try {
            copyFileToStorage(file, targetFile, originalFilename);

            SaleItem saleItem = saleItemRepository.findById(saleItemId)
                    .orElseThrow(() -> new FileStorageException("SaleItem not found with ID: " + saleItemId));


            SaleItemImage image = createSaleItemImage(saleItem, uniqueFilename, order);
            return saleItemImageRepository.save(image);

        } catch (IOException e) {
            cleanupFile(targetFile, uniqueFilename);
            throw new FileStorageException("Could not store file " + originalFilename + " at " + targetFile, e);
        }
    }

    @Transactional
    public String saveUpdate(MultipartFile file, Integer saleItemId, Integer order) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String uniqueFilename = formatFileName(saleItemId, order, originalFilename);
        Path targetFile = baseStoragePath.resolve(uniqueFilename);

        log.info(String.format("Uploading file: %s for SaleItem ID: %d", originalFilename, saleItemId));

        try {
            copyFileToStorage(file, targetFile, originalFilename);

            SaleItem saleItem = saleItemRepository.findById(saleItemId)
                    .orElseThrow(() -> new FileStorageException("SaleItem not found with ID: " + saleItemId));

            return uniqueFilename;

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
        List<SaleItemImage> images = saleItemImageRepository.findAllBySaleItemId(id);
        images.forEach(image -> {
            Path filePath = baseStoragePath.resolve(image.getFileName()).normalize();
            try {
                Files.deleteIfExists(filePath);
                saleItemImageRepository.delete(image);
                log.info("Deleted image with ID: " + id);
            } catch (IOException e) {
                throw new FileStorageException("Could not delete file: " + image.getFileName(), e);
            }
        });
    }



    /**
     * ✅ Checks if a file exists in storage
     *
     * @param filename the filename to check
     * @return true if file exists
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
     *  Deletes a specific image file from storage
     *
     * @param filename the filename of the image to delete
     * @throws FileStorageException if deletion fails
     */
    public void deleteSpecificImageFile(String filename) {
        if (filename == null || filename.trim().isEmpty()) {
            log.warning("Cannot delete file: filename is null or empty");
            return;
        }

        Path filePath = baseStoragePath.resolve(filename).normalize();

        // Security check
        if (!filePath.startsWith(baseStoragePath)) {
            throw new FileStorageException("Invalid file path: " + filename);
        }

        try {
            if (Files.exists(filePath)) {
                Files.delete(filePath);
                log.info("Deleted image file: " + filename);
            } else {
                log.warning("Image file not found for deletion: " + filename);
            }
        } catch (IOException e) {
            throw new FileStorageException("Could not delete file: " + filename, e);
        }
    }

    public void deleteMultipleFiles(List<String> filenames) {
        if (filenames == null || filenames.isEmpty()) {
            return;
        }

        // ใช้ parallel stream สำหรับ I/O operations
        List<String> failedDeletes = filenames.parallelStream()
                .filter(filename -> {
                    try {
                        Path filePath = baseStoragePath.resolve(filename).normalize();
                        if (Files.exists(filePath)) {
                            Files.delete(filePath);
                            log.info("Deleted image file: " + filename);
                            return false; // สำเร็จ
                        }
                        return false;
                    } catch (IOException e) {
                        log.warning("Failed to delete file: " + filename + " - " + e.getMessage());
                        return true; // ล้มเหลว
                    }
                })
                .collect(Collectors.toList());

        if (!failedDeletes.isEmpty()) {
            throw new FileStorageException("Failed to delete some files: " + failedDeletes);
        }
    }

//    file validation เอาไว้ลด overhaed
    public void validateMultipleFiles(List<MultipartFile> files) {
        if(files == null || files.isEmpty()) {
            return ;
        }

        List<String> validationErrors = new ArrayList<>();
        long totalSize = 0;

        for (MultipartFile file : files) {
            if(file == null || file.isEmpty()){
                validationErrors.add("Empty file detected");
                continue;
            }
            totalSize += file.getSize();
            if(file.getSize() > MAX_FILE_SIZE) {
                validationErrors.add("File" + file.getOriginalFilename() + " exceeds size limit");
            }

            String extension = getFileExtension(file.getOriginalFilename()).toLowerCase();
            if(!ALLOWED_EXTENSIONS.contains(extension)){
                validationErrors.add("Invalid file type : " + extension);
            }

            String contentType = file.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                validationErrors.add("Non-image file detected: " + file.getOriginalFilename());
            }
        }

        if(totalSize > MAX_FILE_SIZE * 10) {
            validationErrors.add("Total upload size too large"); //เช่นห้ามเกิน 50MB
        }

        if(!validationErrors.isEmpty()) {
            throw new FileStorageException("Validation errors: " + String.join(", ", validationErrors));
        }

    }
    
    // สำหรับไฟล์ที่โคตรใหญ่
    @Async("fileTaskExecutor")
    public CompletableFuture<Void> deleteFileAsync(List<String> filenames) {
        return CompletableFuture.runAsync(() -> deleteMultipleFiles(filenames));
    }


    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileStorageException("File must not be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileStorageException("File size exceeds limit of 5MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        // validateFileName(originalFilename);

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new FileStorageException("Invalid file type: ." + extension + " is not supported.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new FileStorageException("Only image files are allowed.");
        }
    }

    /**
     */
    private void copyFileToStorageOptimized(MultipartFile file, Path targetFile, String originalFilename) throws IOException {
        // ใช้ transferTo() ซึ่งมี performance ดีกว่า
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            log.info("Successfully copied file: " + originalFilename + " to " + targetFile);
        }
    }

    /**
     */
    public Map<String, Boolean> checkMultipleFilesExist(List<String> filenames) {
        return filenames.parallelStream()
                .collect(Collectors.toMap(
                        filename -> filename,
                        this::fileExists
                ));
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



    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0) {
            throw new FileStorageException("File does not have an extension: " + filename);
        }
        return filename.substring(dotIndex + 1);
    }


    private void copyFileToStorage(MultipartFile file, Path targetFile, String originalFilename) throws IOException {
        Files.copy(file.getInputStream(), targetFile, StandardCopyOption.REPLACE_EXISTING);
        log.info("Successfully copied file: " + originalFilename + " to " + targetFile);
    }

//    private void updateNonPrimaryImages(Integer saleItemId) {
//        saleItemImageRepository.updateAllNonPrimaryBySaleItemId(saleItemId);
//    }

    private SaleItemImage createSaleItemImage(SaleItem saleItem, String uniqueFilename, Integer order) {
        SaleItemImage image = new SaleItemImage();
        image.setSaleItem(saleItem);
        image.setFileName(uniqueFilename);
        image.setImageViewOrder(order);
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

    public boolean deleteByFileName(String fileName) throws IOException {
        Path path = baseStoragePath.resolve(fileName).normalize();
        System.out.println("Deleting file at path: " + path);
        return Files.deleteIfExists(path);
    }
    public String updateFileName(String fileName, Integer id, Integer order) throws IOException {
        if (fileName == null || fileName.isEmpty()) {
            throw new IllegalArgumentException("File is empty or null");
        }

        String newFileName = formatFileName(id, order, fileName);

        System.out.println("originalFileName: " + fileName);
        System.out.println("newFileName: " + newFileName);

        Path oldFilePath = baseStoragePath.resolve(fileName).normalize();
        if (!Files.exists(oldFilePath)) {
            throw new IOException("File not found: " + oldFilePath);
        }

        Path newFilePath = Path.of(fileStorageProperties.getUploadDir()).resolve(newFileName).normalize();

        // ใช้ move แทน copy
        Files.move(oldFilePath, newFilePath, StandardCopyOption.REPLACE_EXISTING);

        return newFileName;
    }

}