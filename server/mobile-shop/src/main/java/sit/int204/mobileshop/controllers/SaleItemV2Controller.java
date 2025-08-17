package sit.int204.mobileshop.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import sit.int204.mobileshop.config.FileStorageProperties;
import sit.int204.mobileshop.dtos.*;


import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.entities.SaleItemImage;
import sit.int204.mobileshop.services.SaleItemImageService;
import sit.int204.mobileshop.services.SaleItemService;
import sit.int204.mobileshop.utils.ListMapper;

@CrossOrigin(origins = "${app.origins}")
@RestController
@RequestMapping("/v2/sale-items")
@Tag(name = "Sale Item API V2", description = "API for retrieving paginated product data")
public class SaleItemV2Controller {
    @Autowired
    private SaleItemService saleItemService;

    @Autowired
    private SaleItemImageService saleItemImageService;

    @Autowired
    FileStorageProperties fileStorageProperties;


    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;



    @PostMapping("")
    public ResponseEntity<SaleItemDetailDto> createSaleItem(
            @ModelAttribute SaleItemRequestDto dto,
            @RequestParam(required = false) List<MultipartFile> images
    ) throws IOException {
        return ResponseEntity.ok(saleItemService.createSaleItem(dto, images));
    }

//    @PatchMapping("/{id}")
//    public ResponseEntity<SaleItemDetailDto> updateSaleItem(@RequestPart(value = "images", required = false) List<MultipartFile> imagesInteger, Integer saleItemId) {
//        saleItemImageService
//    }


    @GetMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> getProductById(
            @Parameter(description = "ID of the product to search", required = true)
            @PathVariable Integer id) {
//        SaleItem item = saleItemService.getSaleItemById(id);

//        SaleItemDetailDto dto = modelMapper.map(item, SaleItemDetailDto.class);
        SaleItem saleItem = modelMapper.map(saleItemService.getSaleItemById(id), SaleItem.class);
        SaleItemDetailDto dto =  saleItemService.getSaleItemById(id);
        dto.setBrandName(saleItem.getBrand().getName());
        return ResponseEntity.ok(dto);
    }

//    @DeleteMapping("/{id}")
//    public ResponseEntity<SaleItemDetailDto> deleteSaleItemPictures(@PathVariable Integer id) {
//        saleItemService.deleteSaleItemById(id);
//        saleItemImageService.removeImages(id);
//        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
//    }

    @PostMapping("/{id}/images")
    public ResponseEntity<?> addImagesToSaleItem(
            @PathVariable Integer id,
            @RequestParam("images") List<MultipartFile> images) {

        try {
            // ตรวจสอบว่า Sale Item มีอยู่จริง (เรียก existing API)
            // สามารถเรียก service หรือ repository ที่มีอยู่

            boolean result = saleItemImageService.addImagesToExistingSaleItem(id, images);

            if (result) {
                // Return updated images data
                Map<String, Object> updatedData = saleItemImageService.getSaleItemWithImages(id);
                return ResponseEntity.ok(updatedData);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to add images"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     *  อัปเดทรูปภาพ (Main endpoint ตาม requirement)
     */
//    @PatchMapping("/{id}/images")
//    public ResponseEntity<?> updateSaleItemImages(
//            @PathVariable Integer id,
//            @RequestPart(value = "imageData", required = false) String imageDataJson,
//            @RequestPart(value = "newImages", required = false) List<MultipartFile> newImages) {
//
//        try {
//            ObjectMapper mapper = new ObjectMapper();
//            UpdateSaleItemPicturesRequest imageData = null;
//
//            if (imageDataJson != null && !imageDataJson.isEmpty()) {
//                imageData = mapper.readValue(imageDataJson, UpdateSaleItemPicturesRequest.class);
//            }
//
//            List<String> imageOrder = imageData != null ? imageData.getImageOrder() : null;
//            List<String> imagesToDelete = imageData != null ? imageData.getImagesToDelete() : null;
//
//            // Check if there are any changes
//            boolean hasChanges = saleItemImageService.hasImageChanges(id, imageOrder, imagesToDelete)
//                    || (newImages != null && !newImages.isEmpty());
//
//            if (!hasChanges) {
//                return ResponseEntity.ok(Map.of("message", "No changes detected"));
//            }
//
//            // Update images
//            boolean result = saleItemImageService.updateImages(id, imageOrder, imagesToDelete, newImages);
//
//            if (result) {
//                // Return updated data in format compatible with existing API
//                Map<String, Object> updatedData = saleItemImageService.getSaleItemWithImages(id);
//
//                // เพิ่มข้อมูลอื่นๆ ที่จำเป็น (ถ้าต้องการ return full sale item)
//                Map<String, Object> response = new HashMap<>();
//                response.put("id", id);
//                response.put("message", "Images updated successfully");
//                response.putAll(updatedData);
//
//                return ResponseEntity.ok(response);
//            } else {
//                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
//                        .body(Map.of("error", "Failed to update images"));
//            }
//
//        } catch (Exception e) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                    .body(Map.of("error", e.getMessage()));
//        }
//    }

    // ยังใช้ไม่ได้
    @PatchMapping("/{id}/images")
    public ResponseEntity<?> updateSaleItemImages(
            @PathVariable Integer id,
            @RequestBody UpdateSaleItemPicturesRequest imageData) {

        List<Integer> imageOrder = imageData.getImageOrder();
        List<Integer> imagesToDelete = imageData.getImagesToDelete();

        boolean hasChanges = saleItemImageService.hasImageChanges(id, imageOrder, imagesToDelete);

        if (!hasChanges) {
            return ResponseEntity.ok(Map.of("message", "No changes detected"));
        }

        boolean result = saleItemImageService.updateImages(id, imageOrder, imagesToDelete, null);

        Map<String, Object> updatedData = saleItemImageService.getSaleItemWithImages(id);
        Map<String, Object> response = new HashMap<>();
        response.put("id", id);
        response.put("message", "Images updated successfully");
        response.putAll(updatedData);

        return ResponseEntity.ok(response);
    }


    /**
     * ✅ ดูรูปภาพของ Sale Item (รองรับ API structure ที่มี)
     */
    @GetMapping("/{id}/images")
    public ResponseEntity<?> getSaleItemImages(@PathVariable Integer id) {
        try {
            List<SaleItemImage> images = saleItemImageService.getSaleItemImages(id);

            // แปลงเป็น format ที่เข้ากับ API response ที่มีอยู่
            List<Map<String, Object>> imageList = images.stream()
                    .map(img -> {
                        Map<String, Object> imageData = new HashMap<>();
                        imageData.put("id", img.getId());
                        imageData.put("fileName", img.getFileName());
                        imageData.put("imageViewOrder", img.getImageViewOrder());
                        imageData.put("imageUrl", "/uploads/" + img.getFileName()); // สำหรับ frontend
                        imageData.put("createdOn", img.getCreatedOn());
                        imageData.put("updatedOn", img.getUpdatedOn());
                        return imageData;
                    })
                    .collect(Collectors.toList());

            Map<String, Object> response = new HashMap<>();
            response.put("saleItemId", id);
            response.put("saleItemImages", imageList);
            response.put("totalImages", imageList.size());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ ลบรูปภาพเฉพาะ
     */
    @DeleteMapping("/{id}/images")
    public ResponseEntity<?> deleteSpecificImages(
            @PathVariable Integer id,
            @RequestBody UpdateSaleItemPicturesRequest imageData) {

        try {
            if (imageData.getImagesToDelete() == null || imageData.getImagesToDelete().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "No images specified for deletion"));
            }

            boolean result = saleItemImageService.deleteSpecificImages(id, imageData.getImagesToDelete());

            if (result) {
                // Return updated images
                Map<String, Object> updatedData = saleItemImageService.getSaleItemWithImages(id);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Images deleted successfully");
                response.put("deletedImages", imageData.getImagesToDelete());
                response.putAll(updatedData);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to delete images"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * ✅ เปลี่ยนลำดับรูปภาพ
     */
    @PatchMapping("/{id}/images/reorder")
    public ResponseEntity<?> reorderImages(
            @PathVariable Integer id,
            @RequestBody UpdateSaleItemPicturesRequest imageData) {

        try {
            if (imageData.getImageOrder() == null || imageData.getImageOrder().isEmpty()) {
                return ResponseEntity.badRequest()
                        .body(Map.of("error", "Image order not specified"));
            }

            boolean result = saleItemImageService.updateImages(id, imageData.getImageOrder(), null, null);

            if (result) {
                Map<String, Object> updatedData = saleItemImageService.getSaleItemWithImages(id);
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Images reordered successfully");
                response.put("newOrder", imageData.getImageOrder());
                response.putAll(updatedData);

                return ResponseEntity.ok(response);
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                        .body(Map.of("error", "Failed to reorder images"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{id}/with-images")
    public ResponseEntity<?> getSaleItemWithImages(@PathVariable Integer id) {
        try {
            // เรียก existing API หรือ service เพื่อดึงข้อมูล Sale Item
            // สมมติว่ามี method getSaleItemById ใน service

            SaleItemDetailDto saleItem = saleItemService.getSaleItemById(id);
            List<SaleItemImage> images = saleItemImageService.getSaleItemImages(id);

            // สร้าง response ที่เข้ากับ format API ที่มีอยู่
            Map<String, Object> response = new HashMap<>();
            response.put("id", saleItem.getId());
            response.put("model", saleItem.getModel()); // หรือ model ถ้ามี
            response.put("brandName", saleItem.getBrandName());
            response.put("description", saleItem.getDescription());
            response.put("quantity", saleItem.getQuantity());
            response.put("price", saleItem.getPrice());
            response.put("createdOn", saleItem.getCreatedOn());
            response.put("updatedOn", saleItem.getUpdatedOn());

            // แปลง images เป็น format ที่ต้องการ
            List<Map<String, Object>> imageList = images.stream()
                    .map(img -> {
                        Map<String, Object> imageData = new HashMap<>();
                        imageData.put("id", img.getId());
                        imageData.put("fileName", img.getFileName());
                        imageData.put("imageViewOrder", img.getImageViewOrder());
                        imageData.put("imageUrl", "/uploads/" + img.getFileName());
                        return imageData;
                    })
                    .collect(Collectors.toList());

            response.put("saleItemImages", imageList);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Sale item not found: " + e.getMessage()));
        }
    }


//
//    @PatchMapping("/{id}")
//    public ResponseEntity<SaleItemDetailDto> updateSaleItemPictures(@PathVariable Integer id, @RequestPart("images") List<MultipartFile> images) {
//        saleItemImageService.updateImages(id, images);
//        return null;
//    }


    @GetMapping("/upload")
    public String getUpload() {
        return  fileStorageProperties.getUploadDir();
    }

    @Operation(summary = "Get paginated products", description = "Retrieve products with pagination, filtering, and sorting options")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved paginated products",
                    content = @Content(schema = @Schema(implementation = PageDto.class))),
            @ApiResponse(responseCode = "400", description = "Missing required parameters",
                    content = @Content)
    })
    @GetMapping("")
    public ResponseEntity<PageDto<SaleItemDto>> getAllSaleItemsPage(
            @Parameter(description = "Page number (zero-based)", required = true)
            @RequestParam(required = true) Integer page,

            @Parameter(description = "Number of items per page", required = true)
            @RequestParam(required = true) Integer size,

            @Parameter(description = "Filter products by brand names", example = "['Apple', 'Samsung']")
            @RequestParam(defaultValue = "[]") List<String> filterBrands,

            @Parameter(description = "Field to sort by", example = "brand.name")
            @RequestParam(defaultValue = "createdOn") String sortField,

            @Parameter(description = "Filter storage size")
            @RequestParam(defaultValue = "[]") List<String> storageSize,

            @Parameter(description = "Price range")
            @RequestParam(required = false) String lowerPrice,

            @Parameter(description = "Price range")
            @RequestParam(required = false) String upperPrice,


            @Parameter(description = "Whether to use exact price matching for single price filter")
            @RequestParam(defaultValue = "false") Boolean isExactPrice,


            @Parameter(description = "Sort direction (asc or desc)")
            @RequestParam(defaultValue = "asc") String sortDirection) throws MissingServletRequestParameterException {

        if (page == null || size == null) {
            throw new MissingServletRequestParameterException(page == null ? "page" : "size", "Integer");
        }

        Integer lower = (lowerPrice == null || lowerPrice.equals("null")) ? null : Integer.valueOf(lowerPrice);
        Integer upper = (upperPrice == null || upperPrice.equals("null")) ? null : Integer.valueOf(upperPrice);

// <<<<<<< pbi15-connect-fe-be-upload-pictures
        PageDto<SaleItemDto> pagedResult = saleItemService.getAllSaleItemsPage(page, size, filterBrands, storageSize, lower, upper, isExactPrice, sortField,
// =======
                // PageDto<SaleItemDto> pagedResult = saleItemService.getAllSaleItemsPage(page, size, filterBrands, storageSize, lower, upper, sortField,

                sortDirection);
        return ResponseEntity.ok(pagedResult);
    }
}