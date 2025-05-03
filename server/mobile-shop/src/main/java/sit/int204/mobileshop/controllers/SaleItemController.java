package sit.int204.mobileshop.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int204.mobileshop.dtos.SaleItemDetailDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.entities.SaleItem;
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

    @GetMapping("")
    public ResponseEntity<List<SaleItemDto>> getAllProducts() {
        List<SaleItem> products = saleItemService.getAllSaleItems();
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(listMapper.toListDto(products, SaleItemDto.class, modelMapper));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SaleItemDetailDto> getProductById(@PathVariable Integer id) {
        return ResponseEntity.ok(modelMapper.map(saleItemService.getSaleItemById(id), SaleItemDetailDto.class));
    }

}