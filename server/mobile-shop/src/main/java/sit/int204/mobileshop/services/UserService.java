package sit.int204.mobileshop.services;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import sit.int204.mobileshop.Role;
import sit.int204.mobileshop.dtos.RegisterUserDto;
import sit.int204.mobileshop.entities.EmailVerificationToken;
import sit.int204.mobileshop.entities.Seller;
import sit.int204.mobileshop.entities.User;
import sit.int204.mobileshop.repositories.EmailVerificationTokenRepository;
import sit.int204.mobileshop.repositories.SellerRepository;
import sit.int204.mobileshop.repositories.UserRepository;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static sit.int204.mobileshop.Role.BUYER;
import static sit.int204.mobileshop.Role.SELLER;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ModelMapper modelMapper;
    private final SellerRepository sellerRepository;
    private final EmailVerificationTokenRepository emailVerificationTokenRepository;
    private final MailService mailService;

    public UserService(UserRepository userRepository,
                       ModelMapper modelMapper,
                       SellerRepository sellerRepository,
                       EmailVerificationTokenRepository tokenRepository,
                       MailService mailService) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
        this.sellerRepository = sellerRepository;
        this.emailVerificationTokenRepository = tokenRepository;
        this.mailService = mailService;
    }

    @Transactional
    public User register(RegisterUserDto dto) {
        User user = modelMapper.map(dto, User.class);
        user.setStatus("INACTIVE"); // ยังไม่ verify
        User savedUser = userRepository.save(user);

        if ("SELLER".equalsIgnoreCase(dto.getRole())) {
            Seller seller = modelMapper.map(dto, Seller.class);
            seller.setUsers(savedUser);
            sellerRepository.save(seller);
        }

        String token = UUID.randomUUID().toString();
        EmailVerificationToken verificationToken = new EmailVerificationToken();
        verificationToken.setUser(savedUser);
        verificationToken.setToken(token);
        verificationToken.setExpiryTime(Instant.now().plus(24, ChronoUnit.HOURS));
        verificationToken.setIsUsed(false);
        verificationToken.setCreatedOn(Instant.now());
        emailVerificationTokenRepository.save(verificationToken);

        sendVerificationEmail(savedUser.getEmail(), token);

        return savedUser;
    }

    @Transactional
    public ResponseEntity<String> verifyUser(String token) {
        EmailVerificationToken verificationToken = emailVerificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid token"));

        if (Boolean.TRUE.equals(verificationToken.getIsUsed())) {
            return ResponseEntity.badRequest().body("Token already used");
        }

        if (verificationToken.getExpiryTime().isBefore(Instant.now())) {
            return ResponseEntity.badRequest().body("Token expired");
        }

        User user = verificationToken.getUser();
        user.setStatus("ACTIVE");
        userRepository.save(user);

        verificationToken.setIsUsed(true);
        emailVerificationTokenRepository.save(verificationToken);

        return ResponseEntity.ok("Email verified successfully");
    }

    private void sendVerificationEmail(String email, String token) {
        mailService.sendVerificationEmail(email,token);
    }
}
