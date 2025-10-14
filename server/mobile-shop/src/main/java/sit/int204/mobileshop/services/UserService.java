package sit.int204.mobileshop.services;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jwt.JWTClaimsSet;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sit.int204.mobileshop.dtos.*;
import sit.int204.mobileshop.entities.*;
import sit.int204.mobileshop.exceptions.EmailAlreadyExistsException;
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

    // ---------------- Register ----------------
    @Transactional
    public UserResponseDto register(RegisterUserDto dto) {
        validateEmailUniqueness(dto.getEmail());

        try {
            processFileUploadsIfSeller(dto);

            User savedUser = createAndSaveUser(dto);
            userRepository.flush();

            sendVerificationEmailAsync(savedUser);

            log.info("User registered successfully with email: {}", savedUser.getEmail());
            return mapToProfileResponseDto(savedUser);

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
        user.setPasswordHash(passwordEncoder.encode(dto.getPassword())); // hash ก่อน save เสมอ
        user.setFullName(dto.getFullname());
        user.setUserType(dto.getRole());
        user.setStatus(UserStatus.INACTIVE.name());

        Instant now = Instant.now();
        user.setCreatedOn(now);
        user.setUpdatedOn(now);

        return userRepository.save(user);
    }

    // ---------------- Authentication ----------------
    @Transactional
    public boolean authenticate(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) return false;

        User u = userOpt.get();
        String storedPassword = u.getPasswordHash();

        if (!isHashedPassword(storedPassword)) {
            if (rawPassword.equals(storedPassword)) {
                migratePassword(u, storedPassword);
                return true;
            } else {
                return false;
            }
        }

        return passwordEncoder.matches(rawPassword, storedPassword);
    }

    @Transactional
    public AuthResponseDto authenticateWithJwt(String email, String rawPassword) {
        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            return null;
        }

        User user = userOpt.get();
        String storedPassword = user.getPasswordHash();

        boolean valid;

        if (!isHashedPassword(storedPassword)) {
            valid = rawPassword.equals(storedPassword);
            log.debug("Compare raw='{}' with stored='{}' → {}", rawPassword, storedPassword, valid);
            if (valid) {
                migratePassword(user, storedPassword);
            }
        } else {
            valid = passwordEncoder.matches(rawPassword, storedPassword);
        }

        if (!valid) {
            return null;
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            return AuthResponseDto.builder().build();
        }

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
        log.debug("Raw input: '{}', Stored: '{}'", rawPassword, storedPassword);


        return AuthResponseDto.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(30 * 60L)
                .nickname(user.getNickName())
                .userId(user.getId())
                .email(user.getEmail())
                .role(user.getUserType())
                .build();

    }

    // ---------------- Email Verification ----------------
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
        UserResponseDto userDto = mapToProfileResponseDto(user);

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

    // ---------------- Profile ----------------
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

        return mapToProfileResponseDto(user);
    }

    @Transactional
    public UserResponseDto updateUserProfile(Long id, UpdateProfileDto updateDto, Authentication authentication) {
        
        if (authentication == null || authentication.getPrincipal() == null) {
            log.error("No authentication provided");
            throw new RuntimeException("No authentication provided");
        }
        UserResponseDto authenticatedUserDto = (UserResponseDto) authentication.getPrincipal();
        if (!authenticatedUserDto.getId().equals(id)) {
            log.error("User ID mismatch: authenticated={}, requested={}", authenticatedUserDto.getId(), id);
            throw new RuntimeException("Request user id not matched with authenticated user");
        }
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found with ID: {}", id);
                    return new RuntimeException("User not found");
                });

        if (!UserStatus.ACTIVE.name().equals(userToUpdate.getStatus())) {
            log.error("User is not active: {}", userToUpdate.getStatus());
            throw new RuntimeException("User is not active");
        }

        log.info("Updating user fields: nickName={}, fullName={}", updateDto.getNickName(), updateDto.getFullName());

        userToUpdate.setNickName(updateDto.getNickName());
        userToUpdate.setFullName(updateDto.getFullName());
        userToUpdate.setUpdatedOn(Instant.now());

        if (UserRole.SELLER.name().equalsIgnoreCase(userToUpdate.getUserType()) &&
                userToUpdate instanceof Seller) {
            Seller seller = (Seller) userToUpdate;

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

        User updatedUser = userRepository.save(userToUpdate);
        log.info("User profile updated successfully for user ID: {}", id);

        return mapToProfileResponseDto(updatedUser);
    }

    private UserResponseDto mapToProfileResponseDto(User user) {
        UserResponseDto response = new UserResponseDto();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setUserType(user.getUserType());
        response.setNickName(user.getNickName());
        response.setStatus(user.getStatus());

        if (UserRole.SELLER.name().equalsIgnoreCase(user.getUserType())) {
            if (user instanceof Seller) {
                Seller seller = (Seller) user;
                response.setMobileNumber(seller.getMobileNumber());
                response.setPhoneNumber(seller.getMobileNumber());
                response.setBankName(seller.getBankName());
                response.setBankAccount(seller.getBankAccountNumber());
            } else {
                Optional<Seller> seller = sellerRepository.findById(user.getId());
                if (seller.isPresent()) {
                    Seller sellerData = seller.get();
                    response.setMobileNumber(sellerData.getMobileNumber());
                    response.setPhoneNumber(sellerData.getMobileNumber());
                    response.setBankName(sellerData.getBankName());
                    response.setBankAccount(sellerData.getBankAccountNumber());
                }
            }
        }

        return response;
    }

    // ---------------- Helpers ----------------
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
        } catch (Exception ignored) {}
    }

    private String getPhoneNumber(User user) {
        if (UserRole.SELLER.name().equalsIgnoreCase(user.getUserType())) {
            if (user instanceof Seller) {
                return ((Seller) user).getMobileNumber();
            } else {
                Optional<Seller> seller = sellerRepository.findById(user.getId());
                return seller.map(Seller::getMobileNumber).orElse(null);
            }
        }
        return null;
    }

    /** ตรวจว่า password ที่เก็บใน DB เป็น hash หรือ plain text */
    private boolean isHashedPassword(String password) {
        return password != null && password.startsWith("$argon2");
    }

    /** migrate password จาก plain text → hash แล้ว update DB */
    private void migratePassword(User user, String plainPassword) {
        try {
            String hashed = passwordEncoder.encode(plainPassword).trim();
            user.setPasswordHash(hashed);
            userRepository.save(user);
            log.info("Migrated plain password for user {} to hash", user.getEmail());
        } catch (Exception e) {
            log.error("Failed to migrate password for {}", user.getEmail(), e);
        }
    }


    // ---------------- Enums ----------------
    public enum UserRole {
        BUYER, SELLER
    }

    public enum UserStatus {
        ACTIVE, INACTIVE
    }

    public AuthResponseDto refreshAccessToken(String refreshToken) {
        try {
            JWTClaimsSet claims = jwtService.validateRefreshToken(refreshToken);
            Long userId = (Long) claims.getClaim("id");
            String email = claims.getStringClaim("email");

            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty() || !"ACTIVE".equals(userOpt.get().getStatus())) {
                return null;
            }
            
            User user = userOpt.get();
            String newAccessToken = jwtService.generateAccessToken(
                user.getId(),
                user.getEmail(),
                user.getNickName(),
                user.getUserType()
            );
            
            return AuthResponseDto.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .expiresIn(30 * 60L)
                .build();
                
        } catch (Exception e) {
            log.error("Failed to refresh token", e);
            return null;
        }
    }

    @Transactional
    public void changePassword(Long userId, String oldPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
        if(!passwordEncoder.matches(oldPassword, user.getPasswordHash())){
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong old password");
        }

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);

    }
}
