package sit.int204.mobileshop.controllers;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.services.SaleItemService;
import sit.int204.mobileshop.dtos.PageDto;
import java.util.List;

@CrossOrigin(origins = "${app.origins}")
@RestController
@RequestMapping("/v2/sale-items")
public class SaleItemV2Controller {
    @Autowired
    private SaleItemService saleItemService;



    @GetMapping("")
    public ResponseEntity<PageDto<SaleItemDto>> getAllSaleItemsPage(
        @RequestParam(defaultValue = "0") Integer page,
        @RequestParam(defaultValue = "5") Integer size,
        @RequestParam(defaultValue = "[]") List<String> filterBrands,
        @RequestParam(defaultValue = "brand.name") String sortField,
        @RequestParam(defaultValue = "asc") String sortDirection) {
        PageDto<SaleItemDto> pagedResult = saleItemService.getAllSaleItemsPage(page, size, filterBrands, sortField, sortDirection);
        return ResponseEntity.ok(pagedResult);
    }
}
