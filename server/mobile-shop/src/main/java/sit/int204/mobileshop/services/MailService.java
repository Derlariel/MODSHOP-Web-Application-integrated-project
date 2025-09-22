package sit.int204.mobileshop.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    private final JavaMailSender mailSender;

    @Value("${app.verifyUrl:http://localhost:8080/itb-mshop/v2/verify-email?token=}")
    private String verifyBaseUrl;

    public MailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendVerificationEmail(String email, String jwtToken) {
        try {
            final String verifyUrl = verifyBaseUrl + jwtToken;

            String subject = "Please Verify Your Email - ITB-Mshop";
            String body = """
                    Welcome to ITB-Mshop!

                    Thank you for registering with us. To activate your account, please click the verification link below:

                    %s

                    This link will expire in 1 hour for security purposes.

                    If you did not create an account with us, please ignore this email.

                    Best regards,
                    ITB-Mshop Team
                    """.formatted(verifyUrl);

            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject(subject);
            msg.setText(body);
            mailSender.send(msg);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }
}
