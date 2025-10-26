package sit.int204.mobileshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import sit.int204.mobileshop.entities.PasswordResetToken;
import sit.int204.mobileshop.entities.User;
import sit.int204.mobileshop.repositories.PasswordResetTokenRepository;
import sit.int204.mobileshop.repositories.UserRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder,
                       JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    // User requests password reset
    @Transactional
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found"));

        String token = UUID.randomUUID().toString();
        Instant expiration = Instant.now().plus(Duration.ofHours(1));

        PasswordResetToken resetToken = new PasswordResetToken();
        resetToken.setUser(user);
        resetToken.setToken(token);
        resetToken.setExpiration(expiration);
        passwordResetTokenRepository.save(resetToken);

        sendPasswordResetEmail(user.getEmail(), token);
    }

    //send email with reset link
    private void sendPasswordResetEmail(String to, String token) {
        String resetLink = frontendBaseUrl + "reset-password?token=" + token;

        SimpleMailMessage mailMessage = new SimpleMailMessage();
        mailMessage.setTo(to);
        mailMessage.setSubject("Password Reset Request");
        mailMessage.setText("""
                You requested to reset your password.
                Please click the link below to reset it:
                %s
                
                This link will expire in 1 hour.
                """.formatted(resetLink));

        mailSender.send(mailMessage);
    }

    @Transactional
    public void changePassword(String email, String oldPassword, String newPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        System.out.println("🧩 [DEBUG] User found: " + user.getEmail());
        System.out.println("🔑 [DEBUG] Old hash: " + user.getPasswordHash());

        if (!passwordEncoder.matches(oldPassword, user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Old password incorrect");
        }

        // ป้องกัน encode hash ซ้ำ
        if (newPassword.startsWith("$2a$") || newPassword.startsWith("$2b$")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid password format");
        }

        String encoded = passwordEncoder.encode(newPassword);
        System.out.println("🧩 [DEBUG] New encoded hash: " + encoded);

        user.setPasswordHash(encoded);
        userRepository.save(user);

        // Clear Security Context 
        org.springframework.security.core.context.SecurityContextHolder.clearContext();
    }

    @Transactional
    public void resetPasswordByToken(String token, String newPassword) {
        PasswordResetToken resetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid or expired token"));

        if (resetToken.isUsed()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token already used");
        }
        if (resetToken.getExpiration().isBefore(Instant.now())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Token expired");
        }

        User user = resetToken.getUser();
        if (user == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }

        String encoded = passwordEncoder.encode(newPassword);
        user.setPasswordHash(encoded);
        userRepository.save(user);

        resetToken.setUsed(true);
        passwordResetTokenRepository.save(resetToken);
    }





}
