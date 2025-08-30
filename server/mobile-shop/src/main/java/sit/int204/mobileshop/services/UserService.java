package sit.int204.mobileshop.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int204.mobileshop.dtos.AuthResponseDto;
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

    private final UserRepository userRepository;
    private final SellerRepository sellerRepository;
    private final ModelMapper modelMapper;
    private final MailService mailService;
    private final JwtService jwtService;
    private final FileService fileService;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       SellerRepository sellerRepository,
                       ModelMapper modelMapper,
                       MailService mailService,
                       JwtService jwtService,
                       FileService fileService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.sellerRepository = sellerRepository;
        this.modelMapper = modelMapper;
        this.mailService = mailService;
        this.jwtService = jwtService;
        this.fileService = fileService;
        this.passwordEncoder = passwordEncoder;

    }

    @Transactional
    public UserResponseDto register(RegisterUserDto dto) {
        validateEmailUniqueness(dto.getEmail());

        try {
            processFileUploadsIfSeller(dto);

            User savedUser = createAndSaveUser(dto);
            userRepository.flush();

            if (UserRole.SELLER.name().equalsIgnoreCase(dto.getRole())) {
                createSellerRecord(dto, savedUser);
            }

            sendVerificationEmailAsync(savedUser);

            log.info("User registered successfully with email: {}", savedUser.getEmail());
            return mapToUserResponseDto(savedUser);

        } catch (Exception e) {
            log.error("Registration failed for user: {}", dto.getEmail(), e);
            throw new RuntimeException("Registration failed: " + e.getMessage());
        }
    }



    private void validateEmailUniqueness(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException("Email address is already registered");
        }
    }

    private void processFileUploadsIfSeller(RegisterUserDto dto) throws IOException {
        if (!UserRole.SELLER.name().equalsIgnoreCase(dto.getRole())) {
            return;
        }
        handleSellerFileUploads(dto);
    }

    private User createAndSaveUser(RegisterUserDto dto) {
        User user = new User();

        user.setNickname(dto.getNickname());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setFullname(dto.getFullname());
        user.setRole(dto.getRole());
        user.setStatus(UserStatus.INACTIVE.name());

        Instant now = Instant.now();
        user.setCreatedOn(now);
        user.setUpdatedOn(now);

        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public boolean authenticate(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;
        User u = userOpt.get();
        return passwordEncoder.matches(rawPassword, u.getPasswordHash());
    }

    @Transactional(readOnly = true)
    public AuthResponseDto authenticateWithJwt(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return null; // User not found
        }
        
        User user = userOpt.get();
        
        // Check password
        if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            return null; // Invalid password
        }
        
        // Check if user is active
        if (!"ACTIVE".equals(user.getStatus())) {
            // Return empty response for inactive user (should result in 403)
            return AuthResponseDto.builder().build();
        }
        
        // Generate tokens
        String accessToken = jwtService.generateAccessToken(
            user.getId(),
            user.getEmail(),
            user.getNickname(),
            user.getRole()
        );
        
        String refreshToken = jwtService.generateRefreshToken(
            user.getId(),
            user.getEmail()
        );
        
        // Create response
        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(30 * 60L) // 30 minutes in seconds
                .nickname(user.getNickname())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }


    private void handleSellerFileUploads(RegisterUserDto dto) throws IOException {
        if (dto.getNationalIdPhotoFront() != null && !dto.getNationalIdPhotoFront().isEmpty()) {
            String frontPath = fileService.storeFile(dto.getNationalIdPhotoFront());
            dto.setNationalIdPhotoFrontUrl(frontPath);
        }

        if (dto.getNationalIdPhotoBack() != null && !dto.getNationalIdPhotoBack().isEmpty()) {
            String backPath = fileService.storeFile(dto.getNationalIdPhotoBack());
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

        Instant now = Instant.now();
        seller.setCreatedOn(now);
        seller.setUpdatedOn(now);

        sellerRepository.save(seller);
    }

    private void sendVerificationEmailAsync(User user) {
        try {
            String jwtToken = jwtService.generateVerificationToken(user.getId(), user.getEmail());
            mailService.sendVerificationEmail(user.getEmail(), jwtToken);
        } catch (Exception e) {

        }
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        UserResponseDto responseDto = modelMapper.map(user, UserResponseDto.class);
        responseDto.setIsActive(UserStatus.ACTIVE.name().equals(user.getStatus()));
        responseDto.setUserType(user.getRole());
        responseDto.setPhoneNumber(getPhoneNumber(user));
        return responseDto;
    }

    @Transactional
    public ResponseEntity<Map<String, Object>> verifyEmail(String jwtToken) {
        try {
            JWTClaimsSet claims = jwtService.validateVerificationToken(jwtToken);
            Long userId = Long.parseLong(claims.getSubject());
            String email = claims.getStringClaim("email");

            Optional<User> userOptional = userRepository.findByIdAndEmail(userId, email);

            if (userOptional.isEmpty()) {
                return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", "Invalid verification token");
            }

            User user = userOptional.get();

            if (UserStatus.ACTIVE.name().equals(user.getStatus())) {
                return buildErrorResponse(HttpStatus.CONFLICT, "Conflict",
                        "Email already verified");
            }

            activateUser(user);

            log.info("User email verified successfully: {}", email);
            return ResponseEntity.ok(buildSuccessResponse(user));

        } catch (ParseException | JOSEException e) {
            log.warn("Invalid verification token: {}", e.getMessage());
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", "Invalid verification token");
        } catch (Exception e) {
            log.error("Error during email verification", e);
            return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                    "An error occurred during verification");
        }
    }

    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String error, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("timestamp", Instant.now().toString());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        response.put("path", "/itb-mshop/v2/users/verify-email");
        return ResponseEntity.status(status).body(response);
    }

    private Map<String, Object> buildSuccessResponse(User user) {
        UserResponseDto userDto = mapToUserResponseDto(user);

        Map<String, Object> response = new HashMap<>();
        response.put("id", userDto.getId());
        response.put("nickname", userDto.getNickname());
        response.put("email", userDto.getEmail());
        response.put("fullName", userDto.getFullName());
        response.put("phoneNumber", userDto.getPhoneNumber());
        response.put("isActive", userDto.getIsActive());
        response.put("userType", userDto.getUserType());

        return response;
    }

    private void activateUser(User user) {
        user.setStatus(UserStatus.ACTIVE.name());
        user.setUpdatedOn(Instant.now());
        userRepository.save(user);
    }

    private String getPhoneNumber(User user) {
        if (UserRole.SELLER.name().equalsIgnoreCase(user.getRole())) {
            Optional<Seller> seller = sellerRepository.findByUsers(user);
            return seller.map(Seller::getMobileNumber).orElse(null);
        }
        return null;
    }

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
            log.error("Error in verifyUser method", e);
            return ResponseEntity.badRequest()
                    .body("An error occurred, or the verification link has expired. Please request a new verification email.");
        }
    }

    public enum UserRole {
        BUYER, SELLER
    }

    public enum UserStatus {
        ACTIVE, INACTIVE
    }
}