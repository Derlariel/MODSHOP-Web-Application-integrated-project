package sit.int204.mobileshop.controllers;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
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
            @RequestParam(required = true) Integer page,
            @RequestParam(required = true) Integer size,
            @RequestParam(defaultValue = "[]") List<String> filterBrands,
            @RequestParam(defaultValue = "createdOn") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection) throws MissingServletRequestParameterException {
        if (page == null || size == null) {
            throw new MissingServletRequestParameterException(page == null ? "page" : "size", "Integer");
        }
        PageDto<SaleItemDto> pagedResult = saleItemService.getAllSaleItemsPage(page, size, filterBrands, sortField,
                sortDirection);
        return ResponseEntity.ok(pagedResult);
    }
}
