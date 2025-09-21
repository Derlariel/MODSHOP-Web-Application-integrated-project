package sit.int204.mobileshop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import sit.int204.mobileshop.dtos.PageDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.services.SaleItemService;

@RestController
@RequestMapping("/v2/sellers/")
public class SellerV2Controller {
    private final SaleItemService saleItemService;

    public SellerV2Controller(SaleItemService saleItemService) {
        this.saleItemService = saleItemService;
    }

    @GetMapping("/{id}/sale-items")
    public ResponseEntity<PageDto<SaleItemDto>> getSaleItems(@PathVariable Long id,
    @RequestParam(required = true) Integer page, @RequestParam(required = true) Integer size,
    @RequestParam(defaultValue = "createdOn") String sortField,  @RequestParam(defaultValue = "asc") String sortDirection) throws MissingServletRequestParameterException {
        if (page == null || size == null) {
            throw new MissingServletRequestParameterException(page == null ? "page" : "size", "Integer");
        }
        PageDto<SaleItemDto> pagedResult = saleItemService.getAllSaleItemsPageBySellerId(id, page, size, sortField, sortDirection);
        return ResponseEntity.ok(pagedResult);
    }
}
