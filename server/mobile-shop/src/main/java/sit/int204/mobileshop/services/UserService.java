package sit.int204.mobileshop.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import sit.int204.mobileshop.dtos.RegisterUserDto;
import sit.int204.mobileshop.dtos.UserResponseDto;
import sit.int204.mobileshop.entities.Seller;
import sit.int204.mobileshop.entities.User;
import sit.int204.mobileshop.exceptions.EmailAlreadyExistsException;
import sit.int204.mobileshop.repositories.SellerRepository;
import sit.int204.mobileshop.repositories.UserRepository;

import java.io.IOException;
import java.text.ParseException;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private SellerRepository sellerRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    private MailService mailService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private FileService fileService;

    @Transactional
    public UserResponseDto register(RegisterUserDto dto) {
        // Check if email already exists
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new EmailAlreadyExistsException("Email address is already registered");
        }

        try {
            // Handle file uploads for seller
            if ("SELLER".equalsIgnoreCase(dto.getRole())) {
                handleSellerFileUploads(dto);
            }

            User user = modelMapper.map(dto, User.class);
            user.setPasswordHash(dto.getPassword()); // Should be encoded in production
            user.setRole(dto.getRole());
            user.setStatus("INACTIVE");
            user.setCreatedOn(Instant.now());
            user.setUpdatedOn(Instant.now());
            User savedUser = userRepository.save(user);

            // Create seller if role is SELLER
            if ("SELLER".equalsIgnoreCase(dto.getRole())) {
                createSellerRecord(dto, savedUser);
            }

            // Generate JWT token for email verification
            String jwtToken = jwtService.generateVerificationToken(savedUser.getId(), savedUser.getEmail());

            // Send verification email
            sendVerificationEmail(savedUser.getEmail(), jwtToken);

            log.info("User registered successfully with email: {}", savedUser.getEmail());

            // Return response DTO
            return UserResponseDto.builder()
                    .id(savedUser.getId())
                    .nickname(savedUser.getNickname())
                    .email(savedUser.getEmail())
                    .fullName(savedUser.getFullname())
                    .phoneNumber(getPhoneNumber(savedUser))
                    .isActive(false)
                    .userType(savedUser.getRole())
                    .build();

        } catch (IOException e) {
            log.error("Failed to process file uploads", e);
            throw new RuntimeException("Failed to process file uploads: " + e.getMessage());
        }
    }

    private void handleSellerFileUploads(RegisterUserDto dto) throws IOException {
        String nationalIdDir = "national-ids";

        // Handle front photo
        if (dto.getNationalIdPhotoFront() != null && !dto.getNationalIdPhotoFront().isEmpty()) {
            String frontPath = fileService.storeFile(dto.getNationalIdPhotoFront(), nationalIdDir);
            dto.setNationalIdPhotoFrontUrl(frontPath);
        }

        // Handle back photo
        if (dto.getNationalIdPhotoBack() != null && !dto.getNationalIdPhotoBack().isEmpty()) {
            String backPath = fileService.storeFile(dto.getNationalIdPhotoBack(), nationalIdDir);
            dto.setNationalIdPhotoBackUrl(backPath);
        }
    }

    private void createSellerRecord(RegisterUserDto dto, User savedUser) {
        Seller seller = new Seller();
        seller.setUsers(savedUser);
        seller.setMobileNumber(dto.getMobileNumber());
        seller.setBankAccountNumber(dto.getBankAccountNumber());
        seller.setBankName(dto.getBankName());
        seller.setNationalIdNumber(dto.getNationalIdNumber());
        seller.setNationalIdPhotoFront(dto.getNationalIdPhotoFrontUrl());
        seller.setNationalIdPhotoBack(dto.getNationalIdPhotoBackUrl());
        seller.setCreatedOn(Instant.now());
        seller.setUpdatedOn(Instant.now());
        sellerRepository.save(seller);
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> verifyEmail(String jwtToken) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Validate JWT token
            JWTClaimsSet claims = jwtService.validateVerificationToken(jwtToken);
            Long userId = Long.parseLong(claims.getSubject());
            String email = claims.getStringClaim("email");

            // Find user by ID and email
            Optional<User> userOptional = userRepository.findByIdAndEmail(userId, email);

            if (userOptional.isEmpty()) {
                response.put("timestamp", Instant.now().toString());
                response.put("status", 400);
                response.put("error", "Bad Request");
                response.put("message", "Invalid verification token");
                response.put("path", "/itb-mshop/v2/users/verify-email");
                return ResponseEntity.badRequest().body(response);
            }

            User user = userOptional.get();

            // Check if user is already active
            if ("ACTIVE".equals(user.getStatus())) {
                response.put("timestamp", Instant.now().toString());
                response.put("status", 409);
                response.put("error", "Conflict");
                response.put("message", "Email already verified (กรณี verify user ที่ active แล้ว)");
                response.put("path", "/itb-mshop/v2/users/verify-email");
                return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
            }

            // Activate user
            user.setStatus("ACTIVE");
            user.setUpdatedOn(Instant.now());
            userRepository.save(user);

            log.info("User email verified successfully: {}", email);

            // Return success response with user data
            response.put("id", user.getId());
            response.put("nickname", user.getNickname());
            response.put("email", user.getEmail());
            response.put("fullName", user.getFullname());
            response.put("phoneNumber", getPhoneNumber(user));
            response.put("isActive", true);
            response.put("userType", user.getRole());

            return ResponseEntity.ok(response);

        } catch (ParseException | JOSEException e) {
            log.warn("Invalid verification token: {}", e.getMessage());
            response.put("timestamp", Instant.now().toString());
            response.put("status", 400);
            response.put("error", "Bad Request");
            response.put("message", "Invalid verification token");
            response.put("path", "/itb-mshop/v2/users/verify-email");
            return ResponseEntity.badRequest().body(response);
        } catch (Exception e) {
            log.error("Error during email verification", e);
            response.put("timestamp", Instant.now().toString());
            response.put("status", 500);
            response.put("error", "Internal Server Error");
            response.put("message", "An error occurred during verification");
            response.put("path", "/itb-mshop/v2/users/verify-email");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }

    private String getPhoneNumber(User user) {
        if ("SELLER".equalsIgnoreCase(user.getRole())) {
            Optional<Seller> seller = sellerRepository.findByUsers(user);
            return seller.map(Seller::getMobileNumber).orElse(null);
        }
        return null;
    }

    // For backward compatibility
    public ResponseEntity<String> verifyUser(String token) {
        try {
            ResponseEntity<Map<String, Object>> response = verifyEmail(token);

            if (response.getStatusCode() == HttpStatus.OK) {
                return ResponseEntity.ok("Your account has been successfully activated.");
            } else {
                Map<String, Object> errorBody = response.getBody();
                String message = errorBody != null ? (String) errorBody.get("message") :
                        "An error occurred, or the verification link has expired. Please request a new verification email.";
                return ResponseEntity.status(response.getStatusCode()).body(message);
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body("An error occurred, or the verification link has expired. Please request a new verification email.");
        }
    }

    private void sendVerificationEmail(String email, String jwtToken) {
        mailService.sendVerificationEmail(email, jwtToken);
    }
}