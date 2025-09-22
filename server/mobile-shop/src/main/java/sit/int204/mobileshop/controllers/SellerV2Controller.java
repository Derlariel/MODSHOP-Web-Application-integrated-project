package sit.int204.mobileshop.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import sit.int204.mobileshop.dtos.PageDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.dtos.UserResponseDto;
import sit.int204.mobileshop.services.SaleItemService;

@RestController
@RequestMapping("/v2/sellers")
public class SellerV2Controller {
    private final SaleItemService saleItemService;

    public SellerV2Controller(SaleItemService saleItemService) {
        this.saleItemService = saleItemService;
    }

    @GetMapping("/{id}/sale-items")
    public ResponseEntity<PageDto<SaleItemDto>> getSaleItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdOn") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            Authentication authentication) {

        try {
            // Get authenticated user from Spring Security context (set by JwtAuthFilter)
            UserResponseDto authenticatedUser = (UserResponseDto) authentication.getPrincipal();
            
            // Check if requested ID matches authenticated user ID (authorization)
            if (!authenticatedUser.getId().equals(id)) {
                throw new RuntimeException("Request seller id not matched with id in access token");
            }
            
            // Validate user status (additional check, though JwtAuthFilter should handle this)
            if (!"ACTIVE".equals(authenticatedUser.getStatus())) {
                throw new RuntimeException("User is not active");
            }
            
            if (!"SELLER".equals(authenticatedUser.getUserType())) {
                throw new RuntimeException("User is not a seller");
            }
            
            // Validate parameters
            if (page < 0) {
                page = 0;
            }
            if (size <= 0 || size > 100) {
                size = 10;
            }

            PageDto<SaleItemDto> pagedResult = saleItemService.getAllSaleItemsPageBySellerId(id, page, size, sortField, sortDirection);
            return ResponseEntity.ok(pagedResult);
            
        } catch (RuntimeException e) {
            // Re-throw authorization/authentication exceptions
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Invalid token or user not found");
        }
    }
}
