package sit.int204.mobileshop.controllers;

import com.nimbusds.jwt.JWTClaimsSet;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import sit.int204.mobileshop.dtos.PageDto;
import sit.int204.mobileshop.dtos.SaleItemDto;
import sit.int204.mobileshop.dtos.UserResponseDto;
import sit.int204.mobileshop.services.JwtService;
import sit.int204.mobileshop.services.SaleItemService;
import sit.int204.mobileshop.services.UserService;

@RestController
@RequestMapping("/v2/sellers")
public class SellerV2Controller {
    private final SaleItemService saleItemService;
    private final UserService userService;
    private final JwtService jwtService;

    public SellerV2Controller(SaleItemService saleItemService, UserService userService, JwtService jwtService) {
        this.saleItemService = saleItemService;
        this.userService = userService;
        this.jwtService = jwtService;
    }

    @GetMapping("/{id}/sale-items")
    public ResponseEntity<PageDto<SaleItemDto>> getSaleItems(
            @PathVariable Long id,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(defaultValue = "createdOn") String sortField,
            @RequestParam(defaultValue = "asc") String sortDirection,
            @RequestHeader("Authorization") String authHeader) {

        // Check if authorization header is present
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("No access token provided");
        }
        
        try {
            // Validate JWT token and extract user ID
            String jwtToken = authHeader.substring(7); // Remove "Bearer " prefix
            JWTClaimsSet claims = jwtService.validateAccessToken(jwtToken);
            if (claims == null) {
                throw new RuntimeException("Invalid token");
            }
            
            Long tokenUserId = Long.parseLong(claims.getSubject());
            
            // Check if requested ID matches authenticated user ID (authorization)
            if (!tokenUserId.equals(id)) {
                throw new RuntimeException("Request seller id not matched with id in access token");
            }
            
            // Get user and validate status
            UserResponseDto user = userService.getUserById(tokenUserId);
            if (user == null) {
                throw new RuntimeException("User not found, Invalid Token");
            }
            
            if (!"ACTIVE".equals(user.getStatus())) {
                throw new RuntimeException("User is not active");
            }
            
            if (!"SELLER".equals(user.getUserType())) {
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
