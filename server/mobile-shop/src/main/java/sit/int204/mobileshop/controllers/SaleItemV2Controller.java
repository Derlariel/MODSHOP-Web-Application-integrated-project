package sit.int204.mobileshop.controllers;

import java.io.IOException;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
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
    private FileStorageProperties fileStorageProperties;

    @Autowired
    private SaleItemImageService saleItemImageService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;


//    @PutMapping("/{id}")
//    public ResponseEntity<SaleItemDetailDto> updateSaleItem(@PathVariable Integer id, @ModelAttribute SaleItemWithImageInfo saleItem) throws IOException {
//        System.out.println(saleItem);
//        saleItemService.updateSaleItemByIdWithImages(id, saleItem);
//
//        return ResponseEntity.ok().body(modelMapper.map(saleItem, SaleItemDetailDto.class));
//    }

    @PostMapping("")
    public ResponseEntity<SaleItemDetailDto> createSaleItem(
            @ModelAttribute SaleItemRequestDto dto,
            @RequestParam(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {
        SaleItemDetailDto result = saleItemService.createSaleItem(dto, images);
        result.setSaleItemImages(saleItemImageService.getSaleItemImagesDto(result.getId()));
        return ResponseEntity.ok(result);
    }

//    @PostMapping("")
//    public ResponseEntity<SaleItemDetailDto> createSaleItem(
//            @RequestPart("dto") SaleItemRequestDto dto,
//            @RequestPart(value = "images", required = false) List<MultipartFile> images
//    ) throws IOException {
//        SaleItemDetailDto result = saleItemService.createSaleItem(dto, images);
//        result.setSaleItemImages(saleItemImageService.getSaleItemImagesDto(result.getId()));
//        return ResponseEntity.ok(result);
//    }


    @GetMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> getProductById(@PathVariable Integer id) {
        SaleItemDetailDto dto = saleItemService.getSaleItemById(id);
        dto.setSaleItemImages(saleItemImageService.getSaleItemImagesDto(id));
        return ResponseEntity.ok(dto);
    }


    @PutMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> updateSaleItem(
            @PathVariable Integer id,
            @ModelAttribute SaleItemRequestDto dto,
            @RequestParam(required = false) List<MultipartFile> images,
            @RequestParam(required = false) List<Integer> imagesToDelete,
            @RequestParam(required = false) List<Integer> imageOrder
    ) throws IOException {
        SaleItemDetailDto result = saleItemService.getSaleItemById(id);
        saleItemImageService.updateImages(id, imageOrder, imagesToDelete, images);
        result.setSaleItemImages(saleItemImageService.getSaleItemImagesDto(id));
        return ResponseEntity.ok(result);
    }


    @DeleteMapping("/{id}/images")
    public ResponseEntity<SaleItemDetailDto> deleteImages(
            @PathVariable Integer id,
            @RequestParam List<Integer> imageIds
    ) {
        saleItemImageService.updateImages(id, null, imageIds, null);
        SaleItemDetailDto result = saleItemService.getSaleItemById(id);
        result.setSaleItemImages(saleItemImageService.getSaleItemImagesDto(id));
        return ResponseEntity.ok(result);
    }

    @GetMapping("/upload")
    public String getUpload() {
        return fileStorageProperties.getUploadDir();
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

            @Parameter(description = "Price range - lower bound")
            @RequestParam(required = false) String lowerPrice,

            @Parameter(description = "Price range - upper bound")
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

        PageDto<SaleItemDto> pagedResult = saleItemService.getAllSaleItemsPage(
                page, size, filterBrands, storageSize, lower, upper, isExactPrice, sortField, sortDirection);

        return ResponseEntity.ok(pagedResult);
    }
}