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
        String verifyUrl = environment.getProperty("app.verifyUrl") + token;
        String subject = "Please Verify Your Email";
        String body = "Click the link below to verify your account:\n" + verifyUrl;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(body);
        this.javaMailSender.send(message);
    }
}
