package sit.int204.mobileshop.controllers;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.services.SaleItemService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.PageRequest;
import sit.int204.mobileshop.dtos.PageDto;
import sit.int204.mobileshop.utils.*;
import org.springframework.data.domain.Page;
import org.modelmapper.ModelMapper;

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
        @RequestParam(defaultValue = "10") Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        PageDto<SaleItemDto> pagedResult = saleItemService.getAllSaleItemsPage(pageable);
        return ResponseEntity.ok(pagedResult);
    }
}
