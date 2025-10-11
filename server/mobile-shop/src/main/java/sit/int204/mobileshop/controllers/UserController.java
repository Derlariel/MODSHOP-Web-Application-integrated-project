package sit.int204.mobileshop.controllers;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.hibernate.query.Page;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import sit.int204.mobileshop.dtos.*;
import sit.int204.mobileshop.dtos.UpdateProfileDto;
import sit.int204.mobileshop.OrderStatus;
import sit.int204.mobileshop.services.OrderService;
import sit.int204.mobileshop.services.UserService;

@RestController
@CrossOrigin(origins = "${app.origins}")
@RequestMapping("/v2/users")
@Tag(name = "User API", description = "API for user registration and email verification")
public class UserController {

    @Autowired
    private Environment env;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderService orderService;


    @GetMapping("/{id}/orders")
    public ResponseEntity<Optional<PageDto<OrderResponseDto>>> getOrders(@PathVariable("id") Long userId,
                                                                         @RequestParam(defaultValue = "0") Integer page,
                                                                         @RequestParam(defaultValue = "10") Integer size,
                                                                         @RequestParam(defaultValue = "orderDate") String sortField,
                                                                         @RequestParam(defaultValue = "desc") String sortDirection) {
        return ResponseEntity.ofNullable(orderService.findByUserId(userId, page, size, sortField, sortDirection));
    }

    @GetMapping("/{id}/orders/status/{status}")
    public ResponseEntity<Optional<PageDto<OrderResponseDto>>> getOrdersByStatus(@PathVariable("id") Long userId,
                                                                                 @PathVariable("status") String status,
                                                                                 @RequestParam(defaultValue = "0") Integer page,
                                                                                 @RequestParam(defaultValue = "10") Integer size,
                                                                                 @RequestParam(defaultValue = "orderDate") String sortField,
                                                                                 @RequestParam(defaultValue = "desc") String sortDirection) {
        OrderStatus orderStatus;
        try {
            orderStatus = OrderStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }

        Optional<PageDto<OrderResponseDto>> result;

        if(!status.equals("ALL")) {
            result = orderService.findByUserIdAndStatus(userId, orderStatus, page, size, sortField, sortDirection);
        }else {
            result = orderService.findByUserId(userId, page, size, sortField, sortDirection);
        }
        return ResponseEntity.ofNullable(result);
    }

    /* End-point รับ user id ตาม principle ถึงแม้จะมี id อยู่ใน access token แล้วก็ตาม (id ใน token ใช้ verify ก่อนจะมาถึง controller)
    Return User (Buyer) หรือ Seller ที่ตรงกับจาก userType (จะเขียน code ง่ายขึ้น ถ้า Seller extends User)
    ** ปรับ requirement จาก slide โดย ตัดเลขที่บัตรประชาชน. รูปบัตร หน้า/หลัง ออก เพราะเป็นข้อมูลสำหรับ role อื่นเช่น platform admin**. */
    @Operation(summary = "Get user profile", description = "Get user profile by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User profile retrieved successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "User not found, Invalid Token", content = @Content),
            @ApiResponse(responseCode = "403", description = "User is not active OR request user id not matched with id in access token", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable Long id,
                                                       Authentication authentication) {
        UserResponseDto user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    @Operation(summary = "Update user profile", description = "Update user profile (nickname and fullname only)")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Profile updated successfully",
                    content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
            @ApiResponse(responseCode = "401", description = "User not found, Invalid Token", content = @Content),
            @ApiResponse(responseCode = "403", description = "User is not active OR request user id not matched with id in access token", content = @Content)
    })
    @PutMapping("/{id}")
    public ResponseEntity<UserResponseDto> updateProfile(@PathVariable Long id,
                                                         @Valid @RequestBody UpdateProfileDto updateDto,
                                                         Authentication authentication) {
        UserResponseDto updatedUser = userService.updateUserProfile(id, updateDto, authentication);
        return ResponseEntity.ok(updatedUser);
    }

}