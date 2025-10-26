package sit.int204.mobileshop.controllers;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;
import sit.int204.mobileshop.dtos.*;
import sit.int204.mobileshop.entities.User;
import sit.int204.mobileshop.services.AuthService;
import sit.int204.mobileshop.services.UserService;

@RestController
@RequestMapping("/v2/auth")
public class AuthController {

    @Autowired
    private UserService userService;

    @Value("${app.cookie.secure:true}")
    private boolean cookieSecure;

    @Value("${app.cookie.same-site:None}")
    private String cookieSameSite;

    @Autowired
    private AuthService authService;


    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletResponse response) {
        // Remove access_token cookie
        Cookie accessTokenCookie = new Cookie("access_token", "");
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(0);
        response.addCookie(accessTokenCookie);

        // Remove refresh_token cookie
        Cookie refreshTokenCookie = new Cookie("refresh_token", "");
        refreshTokenCookie.setHttpOnly(true);
        refreshTokenCookie.setPath("/");
        refreshTokenCookie.setMaxAge(0);
        response.addCookie(refreshTokenCookie);

        return ResponseEntity.noContent().build();
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
        Map<String, Object> result = userService.verifyEmail(token).getBody();
        response.sendRedirect("http://intproj24.sit.kmutt.ac.th/kk1/sale-items?status=verified");
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
    public ResponseEntity<AuthResponseDto> authenticate(@Valid @RequestBody AuthRequestDto req, HttpServletResponse response) {
        AuthResponseDto authResponse = userService.authenticateWithJwt(
                req.getEmail(),
                req.getPassword());

        if (authResponse == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        if (authResponse.getAccessToken() == null) {
            System.out.println("accessToken is null");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        // Access Token อายุ 24 ชั่วโมง
        Cookie accessTokenCookie = new Cookie("access_token", authResponse.getAccessToken());
        accessTokenCookie.setHttpOnly(true);
        accessTokenCookie.setSecure(cookieSecure);
        accessTokenCookie.setPath("/");
        accessTokenCookie.setMaxAge(24 * 60 * 60);  // 24 ชั่วโมง
        response.addCookie(accessTokenCookie);

        // Refresh Token อายุ 7 วัน
        if (authResponse.getRefreshToken() != null) {
            Cookie refreshTokenCookie = new Cookie("refresh_token", authResponse.getRefreshToken());
            refreshTokenCookie.setHttpOnly(true);
            refreshTokenCookie.setSecure(cookieSecure);
            refreshTokenCookie.setPath("/");
            refreshTokenCookie.setMaxAge(7 * 24 * 60 * 60);  // 7 วัน
            response.addCookie(refreshTokenCookie);
        }

        AuthResponseDto responseWithoutTokens = AuthResponseDto.builder()
                .tokenType(authResponse.getTokenType())
                .expiresIn(authResponse.getExpiresIn())
                .nickname(authResponse.getNickname())
                .userId(authResponse.getUserId())
                .email(authResponse.getEmail())
                .role(authResponse.getRole())
                .build();

        return ResponseEntity.ok(responseWithoutTokens); 
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token", description = "Refresh access token using refresh token cookie")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid refresh token")
    })
    public ResponseEntity<Map<String, String>> refreshToken(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;

        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                    break;
                }
            }
        }

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        try {
            AuthResponseDto newTokens = userService.refreshAccessToken(refreshToken);

            if (newTokens == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
            // Access Token อายุ 24 ชั่วโมง
            Cookie accessTokenCookie = new Cookie("access_token", newTokens.getAccessToken());
            accessTokenCookie.setHttpOnly(true);
            accessTokenCookie.setSecure(cookieSecure);
            accessTokenCookie.setPath("/");
            accessTokenCookie.setMaxAge(24 * 60 * 60);  // 24 ชั่วโมง
            response.addCookie(accessTokenCookie);

            Map<String, String> responseBody = new HashMap<>();
            responseBody.put("message", "Token refreshed successfully");
            System.out.println("refresh here");
            return ResponseEntity.ok(responseBody);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam String email) {
        authService.requestPasswordReset(email);
        return ResponseEntity.ok("Reset link sent to " + email);
    }

    @PutMapping("/change-password")
    public ResponseEntity<String> changePassword(
            @RequestBody Map<String, String> body,
            @AuthenticationPrincipal Object principal
    ) {
        String oldPassword = body.get("oldPassword");
        String newPassword = body.get("newPassword");

        System.out.println("🔐 Principal: " + principal);

        String email = null;
        if (principal instanceof UserResponseDto userDto) {
            email = userDto.getEmail();
        } else if (principal instanceof User userEntity) {
            email = userEntity.getEmail();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid user principal");
        }

        authService.changePassword(email, oldPassword, newPassword);
        return ResponseEntity.ok("Password changed successfully");
    }

    @PutMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String newPassword = body.get("newPassword");

        if (token == null || newPassword == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token and newPassword are required");
        }

        authService.resetPasswordByToken(token, newPassword);
        return ResponseEntity.ok("Password has been reset successfully");
    }




}