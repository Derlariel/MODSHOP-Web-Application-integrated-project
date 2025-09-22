package sit.int204.mobileshop.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import sit.int204.mobileshop.dtos.PageDto;
import sit.int204.mobileshop.dtos.SaleItemDetailDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.dtos.SaleItemRequestDto;
import sit.int204.mobileshop.services.SaleItemImageService;
import sit.int204.mobileshop.services.SaleItemService;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/v2/sellers")
public class SellerV2Controller {
    private final SaleItemService saleItemService;
    private final SaleItemImageService  saleItemImageService;
    public SellerV2Controller(SaleItemService saleItemService , SaleItemImageService saleItemImageService) {
        this.saleItemService = saleItemService;
        this.saleItemImageService = saleItemImageService;

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

    @PostMapping("/sellers/{id}/sale-items")
    public ResponseEntity<SaleItemDetailDto> createSaleItemBySeller(
            @PathVariable Long id,
            @ModelAttribute SaleItemRequestDto dto,
            @RequestParam(value = "images", required = false) List<MultipartFile> images,
            Authentication authentication
    ) throws IOException {
        Jwt principal = (Jwt) authentication.getPrincipal();
        String role = principal.getClaim("role");
        Long userId = principal.getClaim("id");

        if (!"SELLER".equalsIgnoreCase(role)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        if (!id.equals(userId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        SaleItemDetailDto result = saleItemService.createSaleItem(dto, images);
        result.setSaleItemImages(saleItemImageService.getSaleItemImagesDto(result.getId()));

        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

}
