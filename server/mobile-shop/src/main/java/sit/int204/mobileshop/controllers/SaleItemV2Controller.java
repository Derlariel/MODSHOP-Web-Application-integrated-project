package sit.int204.mobileshop.controllers;

import java.io.IOException;
import java.util.List;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import sit.int204.mobileshop.config.FileStorageProperties;
import sit.int204.mobileshop.dtos.PageDto;
import sit.int204.mobileshop.dtos.SaleItemDetailDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.dtos.SaleItemRequestDto;
import sit.int204.mobileshop.entities.SaleItem;
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

    @DeleteMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> deleteSaleItemPictures(@PathVariable Integer id) {
        saleItemService.deleteSaleItemById(id);
        saleItemImageService.removeImages(id);  
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
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

        PageDto<SaleItemDto> pagedResult = saleItemService.getAllSaleItemsPage(page, size, filterBrands, storageSize, lower, upper, isExactPrice, sortField,
                sortDirection);
        return ResponseEntity.ok(pagedResult);
    }
}
