package sit.int204.mobileshop.services;

import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import sit.int204.mobileshop.entities.SaleItemImage;
import sit.int204.mobileshop.repositories.SaleItemImageRepository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

// ปรับปรุง SaleItemImageService ให้รองรับ structure ที่มีอยู่

@Service
public class SaleItemImageService {
    @Autowired
    private FileService fileService;
    @Autowired
    private SaleItemImageRepository saleItemImageRepository;

    /**
     *  เพิ่มรูปภาพให้ Sale Item ที่มีอยู่แล้ว
     */
    @Transactional
    public boolean addImagesToExistingSaleItem(Integer saleItemId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return true;
        }

        // ตรวจสอบว่า Sale Item มีอยู่หรือไม่
        // (จากข้อมูล API ที่คุณมี ดูเหมือนจะมี sale item ID = 1 อยู่แล้ว)

        // หา order สุดท้าย
        List<SaleItemImage> existingImages = saleItemImageRepository
                .findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId);

        int nextOrder = existingImages.size() + 1;

        // Validate files
        fileService.validateMultipleFiles(images);

        // Save files
        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                fileService.saveFile(image, saleItemId, nextOrder);
                nextOrder++;
            }
        }

        return true;
    }

    /**
     *  ดึงรูปภาพทั้งหมดของ Sale Item (รองรับ API ที่มี)
     */
    public List<SaleItemImage> getSaleItemImages(Integer saleItemId) {
        return saleItemImageRepository.findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId);
    }

    /**
     *  อัปเดทรูปภาพ (ลบ + เรียงใหม่ + เพิ่ม) - Main method
     */
    @Transactional
    public boolean updateImages(Integer saleItemId, List<Integer> updatedImageOrder,
                                List<Integer> imagesToDelete, List<MultipartFile> newImages) {

        // ดึงข้อมูลรูปปัจจุบัน
        List<SaleItemImage> allImages = saleItemImageRepository
                .findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId);

        List<SaleItemImage> imagesToDeleteFromDB = new ArrayList<>();
        List<String> filesToDelete = new ArrayList<>();

        // Step 1: เตรียมข้อมูลสำหรับการลบ
        if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
            for (SaleItemImage image : allImages) {
                if (imagesToDelete.contains(image.getFileName())) {
                    imagesToDeleteFromDB.add(image);
                    filesToDelete.add(image.getFileName());
                }
            }

            // ลบ files และ database records
            if (!filesToDelete.isEmpty()) {
                fileService.deleteMultipleFiles(filesToDelete);
                saleItemImageRepository.deleteAll(imagesToDeleteFromDB);
            }

            // Remove จาก memory list
            allImages.removeAll(imagesToDeleteFromDB);
        }

        // Step 2: อัปเดท order
        if (updatedImageOrder != null && !updatedImageOrder.isEmpty()) {
            List<SaleItemImage> imagesToUpdate = updateImageOrderOptimized(allImages, updatedImageOrder);
            if (!imagesToUpdate.isEmpty()) {
                saleItemImageRepository.saveAll(imagesToUpdate);
            }
        } else {
            // Re-order เพื่อเติมช่องว่าง
            List<SaleItemImage> imagesToUpdate = reorderImagesOptimized(allImages);
            if (!imagesToUpdate.isEmpty()) {
                saleItemImageRepository.saveAll(imagesToUpdate);
            }
        }

        // Step 3: เพิ่มรูปใหม่
        if (newImages != null && !newImages.isEmpty()) {
            addImagesToExistingSaleItem(saleItemId, newImages);
        }

        return true;
    }

    /**
     *  สร้าง response structure ที่เข้ากับ API ที่มีอยู่
     */
    public Map<String, Object> getSaleItemWithImages(Integer saleItemId) {
        // เรียก API ที่มีอยู่เพื่อดึงข้อมูล Sale Item
        // (ในที่นี้สมมติว่าเรียก service อื่นหรือ repository)

        List<SaleItemImage> images = getSaleItemImages(saleItemId);

        // แปลงเป็น format ที่ตรงกับ API response
        List<Map<String, Object>> imageList = images.stream()
                .map(img -> {
                    Map<String, Object> imageData = new HashMap<>();
                    imageData.put("id", img.getId());
                    imageData.put("fileName", img.getFileName());
                    imageData.put("imageViewOrder", img.getImageViewOrder());
                    imageData.put("createdOn", img.getCreatedOn());
                    imageData.put("updatedOn", img.getUpdatedOn());
                    return imageData;
                })
                .collect(Collectors.toList());

        Map<String, Object> result = new HashMap<>();
        result.put("saleItemImages", imageList);
        result.put("imageCount", imageList.size());

        return result;
    }

    /**
     * ลบรูปเฉพาะ
     */
    @Transactional
    public boolean deleteSpecificImages(Integer saleItemId, List<Integer> imagesToDelete) {
        if (imagesToDelete == null || imagesToDelete.isEmpty()) {
            return true;
        }

        List<SaleItemImage> currentImages = saleItemImageRepository
                .findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId);

        List<SaleItemImage> imagesToDeleteFromDB = new ArrayList<>();
        List<String> filesToDelete = new ArrayList<>();

        // หารูปที่ต้องลบ
        for (SaleItemImage image : currentImages) {
            if (imagesToDelete.contains(image.getId())) {  // ใช้ ID แทน fileName
                imagesToDeleteFromDB.add(image);
                filesToDelete.add(image.getFileName());
            }
        }
//        for (SaleItemImage image : currentImages) {
//            if (imagesToDelete.contains(image.getFileName())) {
//                imagesToDeleteFromDB.add(image);
//                filesToDelete.add(image.getFileName());
//            }
//        }

        // ลบ files และ database records
        if (!filesToDelete.isEmpty()) {
            fileService.deleteMultipleFiles(filesToDelete);
            saleItemImageRepository.deleteAll(imagesToDeleteFromDB);
        }

        // Re-order รูปที่เหลือ
        List<SaleItemImage> remainingImages = currentImages.stream()
                .filter(img -> !imagesToDeleteFromDB.contains(img))
                .collect(Collectors.toList());

        List<SaleItemImage> imagesToUpdate = reorderImagesOptimized(remainingImages);
        if (!imagesToUpdate.isEmpty()) {
            saleItemImageRepository.saveAll(imagesToUpdate);
        }

        return true;
    }

    // Private helper methods (เหมือนเดิม)
    private List<SaleItemImage> reorderImagesOptimized(List<SaleItemImage> images) {
        List<SaleItemImage> imagesToUpdate = new ArrayList<>();

        for (int i = 0; i < images.size(); i++) {
            SaleItemImage image = images.get(i);
            int newOrder = i + 1;

            if (image.getImageViewOrder() != newOrder) {
                image.setImageViewOrder(newOrder);
                image.setUpdatedOn(Instant.now());
                imagesToUpdate.add(image);
            }
        }

        return imagesToUpdate;
    }

//    private List<SaleItemImage> updateImageOrderOptimized(List<SaleItemImage> images, List<Integer> newOrder) {
//        List<SaleItemImage> imagesToUpdate = new ArrayList<>();
//
//        Map<String, SaleItemImage> imageMap = images.stream()
//                .collect(Collectors.toMap(SaleItemImage::getFileName, img -> img));
//
//        for (int i = 0; i < newOrder.size(); i++) {
//            String filename = String.valueOf(newOrder.get(i));
//            SaleItemImage image = imageMap.get(filename);
//
//            if (image != null) {
//                int newOrderValue = i + 1;
//                if (image.getImageViewOrder() != newOrderValue) {
//                    image.setImageViewOrder(newOrderValue);
//                    image.setUpdatedOn(Instant.now());
//                    imagesToUpdate.add(image);
//                }
//            }
//        }
//
//        return imagesToUpdate;
//    }

    private List<SaleItemImage> updateImageOrderOptimized(List<SaleItemImage> images, List<Integer> newOrderIds) {
        List<SaleItemImage> imagesToUpdate = new ArrayList<>();

        Map<Integer, SaleItemImage> imageMap = images.stream()
                .collect(Collectors.toMap(SaleItemImage::getId, img -> img));

        for (int i = 0; i < newOrderIds.size(); i++) {
            Integer id = newOrderIds.get(i);
            SaleItemImage image = imageMap.get(id);

            if (image != null) {
                int newOrderValue = i + 1;
                if (image.getImageViewOrder() != newOrderValue) {
                    image.setImageViewOrder(newOrderValue);
                    image.setUpdatedOn(Instant.now());
                    imagesToUpdate.add(image);
                }
            }
        }

        return imagesToUpdate;
    }
    /**
     * ✅ Check changes
     */
    public boolean hasImageChanges(Integer saleItemId, List<Integer> newImageOrder, List<Integer> imagesToDelete) {
        // Early return สำหรับการลบ
        if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
            return true;
        }

        if (newImageOrder == null || newImageOrder.isEmpty()) {
            return false;
        }

        List<SaleItemImage> currentImages = getSaleItemImages(saleItemId);

        if (currentImages.size() != newImageOrder.size()) {
            return true;
        }

        for (int i = 0; i < currentImages.size(); i++) {
            if (!currentImages.get(i).getFileName().equals(newImageOrder.get(i))) {
                return true;
            }
        }

        return false;
    }
}