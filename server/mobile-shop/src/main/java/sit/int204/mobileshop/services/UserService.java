package sit.int204.mobileshop.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sit.int204.mobileshop.dtos.*;
import sit.int204.mobileshop.entities.*;
import sit.int204.mobileshop.exceptions.*;
import sit.int204.mobileshop.repositories.*;

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
        User user;

        // Create appropriate entity based on role
        if (UserRole.SELLER.name().equalsIgnoreCase(dto.getRole())) {
            Seller seller = new Seller();
            // Set seller-specific fields
            seller.setMobileNumber(dto.getMobileNumber());
            seller.setBankAccountNumber(dto.getBankAccountNumber());
            seller.setBankName(dto.getBankName());
            seller.setNationalIdNumber(dto.getNationalIdNumber());
            seller.setNationalIdPhotoFront(dto.getNationalIdPhotoFrontUrl());
            seller.setNationalIdPhotoBack(dto.getNationalIdPhotoBackUrl());
            user = seller;
        } else {
            user = new User();
        }

        // Set common user fields
        user.setNickName(dto.getNickname());
        user.setEmail(dto.getEmail().trim().toLowerCase());
        //  disabled password hashing ชั่วคราวเ
        // user.setPasswordHash(passwordEncoder.encode(dto.getPassword()));
        user.setPasswordHash(dto.getPassword()); // Store plain text password temporarily
        user.setFullName(dto.getFullname());
        user.setUserType(dto.getRole());
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
        // TODO: Temporarily disabled password hashing for development
        // return passwordEncoder.matches(rawPassword, u.getPasswordHash());
        return rawPassword.equals(u.getPasswordHash()); // Compare plain text passwords temporarily
    }

    @Transactional(readOnly = true)
    public AuthResponseDto authenticateWithJwt(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        
        // Check password
        // TODO: Temporarily disabled password hashing for development
        // if (!passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
        if (!rawPassword.equals(user.getPasswordHash())) { // Compare plain text passwords temporarily
            return null; // Invalid password
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            return AuthResponseDto.builder().build();
        }

        // Generate tokens
        String accessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickName(),
                user.getUserType()
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
                .nickname(user.getNickName())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getUserType())
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

    private void sendVerificationEmailAsync(User user) {
        try {
            String jwtToken = jwtService.generateVerificationToken(user.getId(), user.getEmail());
            mailService.sendVerificationEmail(user.getEmail(), jwtToken);
        } catch (Exception e) {

        }
    }

    private UserResponseDto mapToUserResponseDto(User user) {
        UserResponseDto responseDto = new UserResponseDto();
        responseDto.setId(user.getId());
        responseDto.setEmail(user.getEmail());
        responseDto.setFullName(user.getFullName());
        responseDto.setUserType(user.getUserType());
        responseDto.setNickName(user.getNickName());
        
        // Set both phoneNumber and mobileNumber for sellers
        String phoneNumber = getPhoneNumber(user);
        responseDto.setPhoneNumber(phoneNumber);
        if (UserRole.SELLER.name().equalsIgnoreCase(user.getUserType())) {
            responseDto.setMobileNumber(phoneNumber);
        }
        
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
            return ResponseEntity.ok(buildSuccessResponse(user));

        } catch (ParseException | JOSEException e) {
            return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", "Invalid verification token");
        } catch (Exception e) {
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
        response.put("nickname", userDto.getNickName());
        response.put("email", userDto.getEmail());
        response.put("fullName", userDto.getFullName());
        response.put("phoneNumber", userDto.getPhoneNumber());
        response.put("isActive", UserStatus.ACTIVE.name().equals(user.getStatus()));
        response.put("userType", userDto.getUserType());

        return response;
    }

    private void activateUser(User user) {
        user.setStatus(UserStatus.ACTIVE.name());
        user.setUpdatedOn(Instant.now());
        userRepository.save(user);
    }

    private String getPhoneNumber(User user) {
        if (UserRole.SELLER.name().equalsIgnoreCase(user.getUserType())) {
            // Since Seller extends User, we can cast directly or use repository
            if (user instanceof Seller) {
                return ((Seller) user).getMobileNumber();
            } else {
                // Fallback: Query seller table directly by ID
                Optional<Seller> seller = sellerRepository.findById(user.getId());
                return seller.map(Seller::getMobileNumber).orElse(null);
            }
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
    public UserResponseDto getUserById(Long id) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null) {
            UserResponseDto principal = (UserResponseDto) authentication.getPrincipal();
            if (!principal.getId().equals(id)) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Request user id not matched with id in access token");
            }
        }

        Optional<User> userOpt = userRepository.getUsersById(id);
        User user = userOpt.get();
        
        // Use the existing mapping method that handles seller fields properly
        return mapToProfileResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUserProfile(Long id, UpdateProfileDto updateDto, String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new RuntimeException("No access token provided");
        }

        String token = authHeader.substring(7); // Remove "Bearer " prefix

        try {
            JWTClaimsSet claims = jwtService.validateAccessToken(token);
            String email = claims.getStringClaim("email");
            Long tokenUserId = Long.parseLong(claims.getSubject());

            User authenticatedUser = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found, Invalid Token"));

            // Check if user is active
            if (!UserStatus.ACTIVE.name().equals(authenticatedUser.getStatus())) {
                throw new RuntimeException("User is not active");
            }

            // Check if requested ID matches authenticated user ID (authorization)
            if (!authenticatedUser.getId().equals(id) || !tokenUserId.equals(id)) {
                throw new RuntimeException("Request user id not matched with id in access token");
            }

            // Update only allowed fields (nickname and fullname)
            authenticatedUser.setNickName(updateDto.getNickName());
            authenticatedUser.setFullName(updateDto.getFullName());
            authenticatedUser.setUpdatedOn(Instant.now());

            // If user is a Seller, update seller-specific fields
            if (UserRole.SELLER.name().equalsIgnoreCase(authenticatedUser.getUserType()) &&
                    authenticatedUser instanceof Seller) {
                Seller seller = (Seller) authenticatedUser;

                if (updateDto.getPhoneNumber() != null) {
                    seller.setMobileNumber(updateDto.getPhoneNumber());
                }
                if (updateDto.getBankName() != null) {
                    seller.setBankName(updateDto.getBankName());
                }
                if (updateDto.getBankAccount() != null) {
                    seller.setBankAccountNumber(updateDto.getBankAccount());
                }
            }

            User updatedUser = userRepository.save(authenticatedUser);
            log.info("User profile updated successfully for user ID: {}", id);

            return mapToProfileResponseDto(updatedUser);

        } catch (RuntimeException e) {
            // Re-throw our custom runtime exceptions
            throw e;
        } catch (Exception e) {
            // Handle JWT parsing exceptions (ParseException, JOSEException, etc.)
            throw new RuntimeException("User not found, Invalid Token");
        }
    }

    private UserResponseDto mapToProfileResponseDto(User user) {
        UserResponseDto response = new UserResponseDto();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setUserType(user.getUserType());
        response.setNickName(user.getNickName());
        response.setStatus(user.getStatus()); 

        // If user is a Seller, add seller-specific fields
        if (UserRole.SELLER.name().equalsIgnoreCase(user.getUserType())) {
            // Cast to Seller since we know it's a Seller
            if (user instanceof Seller) {
                Seller seller = (Seller) user;
                response.setMobileNumber(seller.getMobileNumber());
                response.setPhoneNumber(seller.getMobileNumber()); // For backward compatibility
                response.setBankName(seller.getBankName());
                response.setBankAccount(seller.getBankAccountNumber());
            } else {
                // Fallback: Query seller table directly
                Optional<Seller> seller = sellerRepository.findById(user.getId());
                if (seller.isPresent()) {
                    Seller sellerData = seller.get();
                    response.setMobileNumber(sellerData.getMobileNumber());
                    response.setPhoneNumber(sellerData.getMobileNumber()); // For backward compatibility
                    response.setBankName(sellerData.getBankName());
                    response.setBankAccount(sellerData.getBankAccountNumber());
                }
            }
        }

        return response;
    }

    public enum UserRole {
        BUYER, SELLER
    }

    public enum UserStatus {
        ACTIVE, INACTIVE
    }
}