package sit.int204.mobileshop.controllers;

import java.io.IOException;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import sit.int204.mobileshop.dtos.AuthRequestDto;
import sit.int204.mobileshop.dtos.RegisterUserDto;
import sit.int204.mobileshop.dtos.UserResponseDto;
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

        @PostMapping("/authentications")
        public ResponseEntity<?> authenticate(@Valid @RequestBody AuthRequestDto req) {
                boolean ok = userService.authenticate(
                                req.getEmail(),
                                req.getPassword());
                if (ok)
                        return ResponseEntity.ok().build(); // 200
                return ResponseEntity.status(401).build(); // 401 for wrong credentials
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

}