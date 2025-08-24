package sit.int204.mobileshop.controllers;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import sit.int204.mobileshop.dtos.RegisterUserDto;
import sit.int204.mobileshop.entities.User;
import sit.int204.mobileshop.services.UserService;

@RestController
@CrossOrigin(origins = "${app.origins}")
@RequestMapping("/v2/users")
@Tag(name = "User API", description = "API for user registration and email verification")
public class UserController {

    @Autowired
    private UserService userService;

    @Operation(summary = "Register user", description = "Register a new buyer or seller account")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "User registered successfully", 
                    content = @Content(schema = @Schema(implementation = User.class))),
        @ApiResponse(responseCode = "400", description = "Invalid input data", 
                    content = @Content),
        @ApiResponse(responseCode = "409", description = "Email already exists", 
                    content = @Content)
    })
    @PostMapping("/register")
    public ResponseEntity<User> register(
            @Parameter(description = "User registration data", required = true)
            @Valid @RequestBody RegisterUserDto dto) {
        User savedUser = userService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedUser);
    }

    @Operation(summary = "Verify email", description = "Verify user email using verification token")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Email verified successfully", 
                    content = @Content),
        @ApiResponse(responseCode = "400", description = "Invalid or expired token", 
                    content = @Content)
    })
    @GetMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @Parameter(description = "Email verification token", required = true)
            @RequestParam("token") String token) {
        return userService.verifyUser(token);
    }

}
