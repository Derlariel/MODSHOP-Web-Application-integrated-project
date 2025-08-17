package sit.int204.mobileshop.services;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import sit.int204.mobileshop.dtos.UpdateSaleItemPicturesRequest;
import sit.int204.mobileshop.entities.SaleItemImage;
import sit.int204.mobileshop.repositories.SaleItemImageRepository;

@Service
public class SaleItemImageService {
    @Autowired
    private FileService fileService;
    @Autowired
    private SaleItemImageRepository saleItemImageRepository;

    public List<SaleItemImage> getSaleItemImageRepository(Integer saleItemId) {
        return saleItemImageRepository.findAllBySaleItemId(saleItemId);
    }

    public List<SaleItemImage> getSaleItemImages(Integer saleItemId) {
        return saleItemImageRepository.findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId);
    }

    @Transactional
    public List<SaleItemImage> updatePictures(Integer saleItemId, UpdateSaleItemPicturesRequest request) {
        // 1) Delete selected images (physical file + DB record)
        if (request.getDeleteIds() != null && !request.getDeleteIds().isEmpty()) {
            // fetch existing images to delete (so we can get file path / name)
            List<SaleItemImage> toDelete = saleItemImageRepository.findAllById(request.getDeleteIds());
            // delete files (use fileService)
            for (SaleItemImage img : toDelete) {
                try {
                    fileService.deleteImageById(img.getId()); // assume fileService knows how to map id -> filepath
                } catch (Exception ex) {
                    // log and continue - don't break the whole transaction for file deletion issue
                    // (or rethrow if you want strict behavior)
                    System.err.println("Failed to delete file for image id " + img.getId() + ": " + ex.getMessage());
                }
            }
            // delete records in DB
            saleItemImageRepository.deleteAll(toDelete);
        }

        // 2) Reorder remaining images based on request.order if provided
        // We'll produce a deterministic final ordering and ensure contiguous positions 1..N
        List<SaleItemImage> remaining = saleItemImageRepository.findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId);

        if (request.getOrder() != null && !request.getOrder().isEmpty()) {
            // Map imageId -> requested position
            final var posMap = request.getOrder().stream()
                    .collect(Collectors.toMap(UpdateSaleItemPicturesRequest.ImageOrderDto::getImageId,
                            UpdateSaleItemPicturesRequest.ImageOrderDto::getPosition));

            // Apply requested positions to remaining images that are present in posMap
            for (SaleItemImage img : remaining) {
                Integer newPos = posMap.get(img.getId());
                if (newPos != null) {
                    img.setImageViewOrder(newPos);
                }
            }
        }

        // Normalize / compact positions to 1..N (sorted by imageViewOrder then id)
        remaining = remaining.stream()
                .sorted(Comparator.comparingInt(SaleItemImage::getImageViewOrder)
                        .thenComparing(SaleItemImage::getId))
                .collect(Collectors.toList());

        for (int i = 0; i < remaining.size(); i++) {
            remaining.get(i).setImageViewOrder(i + 1);
        }

        // persist changes in batch
        saleItemImageRepository.saveAll(remaining);

        // return updated list to caller (ordered)
        return saleItemImageRepository.findAllBySaleItemIdOrderByImageViewOrderAsc(saleItemId);
    }
}
