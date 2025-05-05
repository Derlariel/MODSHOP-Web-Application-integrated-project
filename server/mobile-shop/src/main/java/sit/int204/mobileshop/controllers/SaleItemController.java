package sit.int204.mobileshop.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import sit.int204.mobileshop.dtos.SaleItemDetailDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.exceptions.MyErrorResponse;
import sit.int204.mobileshop.services.SaleItemService;
import sit.int204.mobileshop.utils.ListMapper;

import java.util.List;

@CrossOrigin(origins = "http://ip24kk1.sit.kmutt.ac.th")
@RestController
@RequestMapping("/v1/sale-items")
public class SaleItemController {
    @Autowired
    private SaleItemService saleItemService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    @Operation(summary = "Get all sale items", description = "Retrieve a list of all available sale items")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved all sale items", content = @Content(schema = @Schema(implementation = SaleItemDto.class))),
            @ApiResponse(responseCode = "204", description = "No sale items found", content = @Content)
    })
    @GetMapping("")
    public ResponseEntity<List<SaleItemDto>> getAllProducts() {
        List<SaleItem> products = saleItemService.getAllSaleItems();
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(listMapper.toListDto(products, SaleItemDto.class, modelMapper));
    }

    @Operation(summary = "Get sale item by ID", description = "Retrieve a sale item by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Successfully retrieved the sale item", content = @Content(schema = @Schema(implementation = SaleItemDetailDto.class))),
            @ApiResponse(responseCode = "404", description = "Sale item not found", content = @Content(schema = @Schema(implementation = MyErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(modelMapper.map(saleItemService.getSaleItemById(id), SaleItemDetailDto.class));
    }

    @Operation(summary = "Delete all sale items", description = "Delete all sale items (for testing purposes only)")
    @ApiResponse(responseCode = "204", description = "All sale items deleted successfully")
    @DeleteMapping("/delete-all")
    public ResponseEntity<Void> deleteAllProducts() {
        saleItemService.deleteAllForTest();
        return ResponseEntity.noContent().build();
    }



}