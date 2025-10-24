package sit.int204.mobileshop.controllers;

import java.io.IOException;
import java.util.List;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import sit.int204.mobileshop.config.FileStorageProperties;
import sit.int204.mobileshop.dtos.SaleItemDetailDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.dtos.SaleItemRequestDto;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.repositories.SaleItemImageRepository;
import sit.int204.mobileshop.services.SaleItemService;
import sit.int204.mobileshop.utils.ListMapper;

@CrossOrigin(origins = "${app.origins}")
@RestController
@RequestMapping("/v1/sale-items")
@Tag(name = "Sale Item API V1", description = "API for managing product data in the system")
public class SaleItemController {
    @Autowired
    private SaleItemService saleItemService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;
    @Autowired
    private FileStorageProperties fileStorageProperties;

    @Autowired
    private SaleItemImageRepository saleItemImageRepository;



    @Operation(summary = "Get all products", description = "Retrieve all products")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Successfully retrieved products", 
                    content = @Content(schema = @Schema(implementation = SaleItemDto.class))),
        @ApiResponse(responseCode = "200", description = "Product is empty",
                    content = @Content)
    })
    @GetMapping("")
    public ResponseEntity<List<SaleItemDto>> getAllProducts() {
        List<SaleItem> products = saleItemService.getAllSaleItems();
        if (products.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(listMapper.mapList(products, SaleItemDto.class, modelMapper));
    }

    @Operation(summary = "Get product by ID", description = "Search and retrieve product using product ID")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product found", 
                    content = @Content(schema = @Schema(implementation = SaleItemDetailDto.class))),
        @ApiResponse(responseCode = "404", description = "Product not found", 
                    content = @Content)
    })
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

    @Operation(summary = "Create new product", description = "Add new product to the system")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Product created successfully", 
                    content = @Content(schema = @Schema(implementation = SaleItemDetailDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                    content = @Content)
    })

    @PostMapping("")
    public ResponseEntity<SaleItemDetailDto> createSaleItem(
            @RequestPart("data") String dtoItem,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        SaleItemRequestDto data = objectMapper.readValue(dtoItem, SaleItemRequestDto.class);

        SaleItem saleItem = modelMapper.map(data, SaleItem.class);

        saleItemService.createSaleItem(data, images);

        SaleItemDetailDto responseDto = modelMapper.map(saleItem, SaleItemDetailDto.class);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }


    @Operation(summary = "Update product", description = "Update existing product information")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Product updated successfully",
                    content = @Content(schema = @Schema(implementation = SaleItemDetailDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data",
                    content = @Content),
        @ApiResponse(responseCode = "404", description = "Product not found",
                    content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> updateSaleItemById(
            @Parameter(description = "ID of the product to update", required = true)
            @PathVariable Integer id,
            @Parameter(description = "Updated product data", required = true)
            @Valid @RequestBody SaleItemRequestDto dtoItem) {
        SaleItemDetailDto updatedItem = saleItemService.updateSaleItemById(id, dtoItem);
        return ResponseEntity.ok(updatedItem);
    }

   @Operation(summary = "Delete product", description = "Delete product by specified ID")
   @ApiResponses({
       @ApiResponse(responseCode = "204", description = "Product deleted successfully",
                   content = @Content),
       @ApiResponse(responseCode = "404", description = "Product not found",
                   content = @Content)
   })
   @DeleteMapping("/{id}")
   public ResponseEntity<Void> deleteSaleItemById(
           @Parameter(description = "ID of the product to delete", required = true)
           @PathVariable Integer id) {
       saleItemService.deleteSaleItemById(id);
       return ResponseEntity.noContent().build();
   }

}