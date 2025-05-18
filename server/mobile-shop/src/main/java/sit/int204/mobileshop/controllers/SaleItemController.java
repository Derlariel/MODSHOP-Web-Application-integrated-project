package sit.int204.mobileshop.controllers;

import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int204.mobileshop.dtos.SaleItemDetailDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.dtos.SaleItemRequestDto;
import sit.int204.mobileshop.entities.SaleItem;
import sit.int204.mobileshop.services.SaleItemService;
import sit.int204.mobileshop.utils.ListMapper;

import java.util.List;

@CrossOrigin(origins = "${app.origins}")
@RestController
@RequestMapping("/v1/sale-items")
public class  SaleItemController {
    @Autowired
    private SaleItemService saleItemService;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private ListMapper listMapper;

    @GetMapping("")
    public ResponseEntity<List<SaleItemDto>> getAllProducts() {
        List<SaleItem> products = saleItemService.getAllSaleItems();
        if (products.isEmpty()) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.ok(listMapper.toListDto(products, SaleItemDto.class, modelMapper));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> getProductById(@PathVariable Integer id) {
        SaleItem item = saleItemService.getSaleItemById(id);
        SaleItemDetailDto dto = modelMapper.map(item, SaleItemDetailDto.class);
        dto.setBrandName(item.getBrand().getName());
        return ResponseEntity.ok(dto);
    }

    @PostMapping("")
    public ResponseEntity<SaleItemDetailDto> createSaleItem(@Valid @RequestBody SaleItemRequestDto dtoItem) {
        SaleItemDetailDto createdItem = saleItemService.createSaleItem(dtoItem);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdItem);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> updateSaleItemById(@PathVariable Integer id,
            @Valid @RequestBody SaleItemRequestDto dtoItem) {
        SaleItemDetailDto updatedItem = saleItemService.updateSaleItemById(id, dtoItem);
        return ResponseEntity.ok(updatedItem);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSaleItemById(@PathVariable Integer id) {
        saleItemService.deleteSaleItemById(id);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/delete/all")
    public ResponseEntity<Void> deleteAllProducts() {
        saleItemService.deleteAllForTest();
        return ResponseEntity.noContent().build();
    }
}