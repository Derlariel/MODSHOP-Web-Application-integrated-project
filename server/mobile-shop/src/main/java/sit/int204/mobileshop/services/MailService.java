package sit.int204.mobileshop.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService {

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private Environment environment;

    protected void sendVerificationEmail(String email, String token) {
        String teamCode = environment.getProperty("app.teamCode", "kk-1");
        String verifyUrl = "http://intproj24.sit.kmutt.ac.th/" + teamCode + "/verify-email/?token=" + token;
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

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);
        this.javaMailSender.send(message);
    }
}
