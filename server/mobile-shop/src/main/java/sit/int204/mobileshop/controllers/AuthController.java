package sit.int204.mobileshop.controllers;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import sit.int204.mobileshop.dtos.AuthRequestDto;
import sit.int204.mobileshop.dtos.AuthResponseDto;
import sit.int204.mobileshop.dtos.RegisterUserDto;
import sit.int204.mobileshop.dtos.UserResponseDto;
import sit.int204.mobileshop.services.UserService;

@RestController
@RequestMapping("/v2/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Remove refresh_token cookie
        Cookie cookie = new Cookie("refresh_token", "");
        cookie.setHttpOnly(true);
        cookie.setPath("/v2/auth"); // Set path as needed
        cookie.setMaxAge(0); // Expire immediately
        response.addCookie(cookie);
        return ResponseEntity.noContent().build(); // 204 No Content
    }

    @Operation(summary = "Register user", description = "Register a new buyer or seller account with file upload support")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully", content = @Content(schema = @Schema(implementation = UserResponseDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data or validation error", content = @Content),
        @ApiResponse(responseCode = "409", description = "Email already exists", content = @Content)
    })
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UserResponseDto> register(
            @Parameter(description = "User registration form data", required = true) @Valid @ModelAttribute RegisterUserDto dto) {
        UserResponseDto response = userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

     @Operation(summary = "Verify email", description = "Verify user email using verification token")
        @ApiResponses({
                        @ApiResponse(responseCode = "200", description = "Email verified successfully", content = @Content),
                        @ApiResponse(responseCode = "400", description = "Invalid or expired token", content = @Content),
                        @ApiResponse(responseCode = "409", description = "Email already verified", content = @Content)
        })
        @GetMapping("/verify-email")
        public ResponseEntity<Map<String, Object>> verifyEmail(
                        @RequestParam("token") String token,
                        HttpServletResponse response) throws IOException {
                // <<<<<<< Updated upstream

                // Map<String, Object> result = userService.verifyEmail(token).getBody();
                // String redirectUrl = env.getProperty("app.frontend.redirect");
                // response.sendRedirect(redirectUrl + "sale-items?status=verified");
                // System.out.println(redirectUrl + "sale-items");
                // =======
                Map<String, Object> result = userService.verifyEmail(token).getBody();
                response.sendRedirect("http://intproj24.sit.kmutt.ac.th/kk1/sale-items?status=verified");
                // >>>>>>> Stashed changes
                return ResponseEntity.ok(result);
        }

        @PostMapping("/login")
        @Operation(summary = "Authenticate user", description = "Authenticate user and return JWT tokens")
        @ApiResponses({
                @ApiResponse(responseCode = "200", description = "Authentication successful", 
                           content = @Content(schema = @Schema(implementation = AuthResponseDto.class))),
                @ApiResponse(responseCode = "401", description = "Invalid credentials", content = @Content),
                @ApiResponse(responseCode = "403", description = "Account not activated", content = @Content)
        })
        public ResponseEntity<AuthResponseDto> authenticate(@Valid @RequestBody AuthRequestDto req) {
                AuthResponseDto authResponse = userService.authenticateWithJwt(
                                req.getEmail(),
                                req.getPassword());
                
                if (authResponse == null) {
                        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build(); // 401
                }
                
                if (authResponse.getAccessToken() == null) {
                    System.out.println("accessToken is null");
                        return ResponseEntity.status(HttpStatus.FORBIDDEN).build(); // 403
                }
                
                // Authentication successful
                return ResponseEntity.ok(authResponse); // 200
        }

}
