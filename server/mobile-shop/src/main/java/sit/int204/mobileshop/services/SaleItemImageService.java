package sit.int204.mobileshop.services;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import sit.int204.mobileshop.dtos.SaleItemImageDto;
import sit.int204.mobileshop.entities.SaleItemImage;
import sit.int204.mobileshop.repositories.SaleItemImageRepository;

@Service
public class SaleItemImageService {
    @Autowired
    private FileService fileService;
    @Autowired
    private SaleItemImageRepository saleItemImageRepository;

    public List<SaleItemImageDto> getSaleItemImagesDto(Integer saleItemId) {
        return saleItemImageRepository.findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId)
                .stream()
                .map(img -> new SaleItemImageDto(img.getId(), img.getFileName(), img.getImageViewOrder()))
                .collect(Collectors.toList());
    }

    @Transactional
    public boolean addImagesToExistingSaleItem(Integer saleItemId, List<MultipartFile> images) {
        if (images == null || images.isEmpty()) {
            return true;
        }

        List<SaleItemImage> existingImages = saleItemImageRepository
                .findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId);

        int nextOrder = existingImages.size() + 1;

        fileService.validateMultipleFiles(images);

        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                // แก้ไข: เรียกใช้ method ที่มีอยู่จริงใน FileService
                // สร้าง SaleItemImageRequest สำหรับแต่ละไฟล์
                sit.int204.mobileshop.dtos.SaleItemImageRequest request =
                        new sit.int204.mobileshop.dtos.SaleItemImageRequest();
                request.setImageFile(image);
                request.setOrder(nextOrder);
                request.setStatus("NEW");

                List<sit.int204.mobileshop.dtos.SaleItemImageRequest> requests =
                        List.of(request);

                List<SaleItemImage> savedImages = fileService.saveImagesWithOrder(requests, saleItemId);

                // อัปเดต order สำหรับรูปถัดไป
                nextOrder++;
            }
        }

        return true;
    }

    @Transactional
    public boolean updateImages(Integer saleItemId, List<Integer> updatedImageOrder,
                                List<Integer> imagesToDelete, List<MultipartFile> newImages) {
        List<SaleItemImage> allImages = saleItemImageRepository
                .findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId);

        // ลบรูปที่ไม่ต้องการ
        if (imagesToDelete != null && !imagesToDelete.isEmpty()) {
            List<SaleItemImage> toDelete = allImages.stream()
                    .filter(img -> imagesToDelete.contains(img.getId()))
                    .toList();

            if (!toDelete.isEmpty()) {
                // แก้ไข: ใช้ method ที่มีอยู่จริง
                List<String> fileNamesToDelete = toDelete.stream()
                        .map(SaleItemImage::getFileName)
                        .toList();

                fileService.deleteSpecificImages(saleItemId, fileNamesToDelete);
                allImages.removeAll(toDelete);
            }
        }

        // อัปเดต order ของรูปที่เหลือ
        if (updatedImageOrder != null && !updatedImageOrder.isEmpty()) {
            List<SaleItemImage> reordered = updateImageOrderOptimized(allImages, updatedImageOrder);
            if (!reordered.isEmpty()) saleItemImageRepository.saveAll(reordered);
        } else {
            List<SaleItemImage> reordered = reorderImagesOptimized(allImages);
            if (!reordered.isEmpty()) saleItemImageRepository.saveAll(reordered);
        }

        // เพิ่มรูปใหม่
        if (newImages != null && !newImages.isEmpty()) {
            addImagesToExistingSaleItem(saleItemId, newImages);
        }

        return true;
    }

    private List<SaleItemImage> updateImageOrderOptimized(List<SaleItemImage> images, List<Integer> newOrderIds) {
        List<SaleItemImage> imagesToUpdate = new ArrayList<>();
        Map<Integer, SaleItemImage> imageMap = images.stream()
                .collect(Collectors.toMap(SaleItemImage::getId, img -> img));

        for (int i = 0; i < newOrderIds.size(); i++) {
            Integer id = newOrderIds.get(i);
            SaleItemImage image = imageMap.get(id);
            if (image != null && image.getImageViewOrder() != i + 1) {
                image.setImageViewOrder(i + 1);
                image.setUpdatedOn(Instant.now());
                imagesToUpdate.add(image);
            }
        }
        return imagesToUpdate;
    }

    @Transactional
    public boolean deleteSpecificImages(Integer saleItemId, List<Integer> imageIds) {
        if (imageIds == null || imageIds.isEmpty()) {
            return true;
        }

        List<SaleItemImage> imagesToDelete = saleItemImageRepository
                .findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId)
                .stream()
                .filter(img -> imageIds.contains(img.getId()))
                .toList();

        if (!imagesToDelete.isEmpty()) {
            // แก้ไข: ใช้ method ที่มีอยู่จริง
            List<String> fileNamesToDelete = imagesToDelete.stream()
                    .map(SaleItemImage::getFileName)
                    .toList();

            fileService.deleteSpecificImages(saleItemId, fileNamesToDelete);

            List<SaleItemImage> remainingImages = saleItemImageRepository
                    .findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId);
            List<SaleItemImage> reordered = reorderImagesOptimized(remainingImages);
            if (!reordered.isEmpty()) {
                saleItemImageRepository.saveAll(reordered);
            }
        }

        return true;
    }

    private List<SaleItemImage> reorderImagesOptimized(List<SaleItemImage> images) {
        List<SaleItemImage> updates = new ArrayList<>();
        for (int i = 0; i < images.size(); i++) {
            SaleItemImage img = images.get(i);
            if (img.getImageViewOrder() != i + 1) {
                img.setImageViewOrder(i + 1);
                img.setUpdatedOn(Instant.now());
                updates.add(img);
            }
        }
        return updates;
    }

    @Transactional
    public List<SaleItemImage> manageImagesComplete(Integer saleItemId,
                                                    List<sit.int204.mobileshop.dtos.SaleItemImageRequest> imageRequests) {
        return fileService.saveImagesWithOrder(imageRequests, saleItemId);
    }

    public List<String> getImageUrls(Integer saleItemId) {
        return fileService.getImageUrls(saleItemId);
    }

    @Transactional
    public void deleteAllImagesForSaleItem(Integer saleItemId) {
        fileService.deleteImagesForSaleItem(saleItemId);
    }
}