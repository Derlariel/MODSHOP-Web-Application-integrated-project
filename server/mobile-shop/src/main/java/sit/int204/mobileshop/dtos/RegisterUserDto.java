package sit.int204.mobileshop.dtos;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import org.springframework.web.multipart.MultipartFile;
import sit.int204.mobileshop.utils.SellerFieldsValidation;

@Data
@SellerFieldsValidation
public class RegisterUserDto {
    @NotBlank(message = "Nickname is required")
    @Size(max = 50, message = "Nickname must not exceed 50 characters")
    private String nickname;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @Size(max = 100, message = "Email must not exceed 100 characters")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min = 4, max = 40, message = "Full name must be between 4 and 40 characters")
    private String fullname;

    @NotBlank(message = "Role is required")
    @Pattern(regexp = "(?i)^(BUYER|SELLER)$", message = "Role must be BUYER or SELLER")
    private String role;

    // Seller specific fields - will be validated by @SellerFieldsValidation
    @Size(max = 20, message = "Mobile number must not exceed 20 characters")
    private String mobileNumber;

    @Size(max = 30, message = "Bank account number must not exceed 30 characters")
    private String bankAccountNumber;

    @Size(max = 100, message = "Bank name must not exceed 100 characters")
    private String bankName;

    @Size(max = 50, message = "National ID number must not exceed 50 characters")
    private String nationalIdNumber;

    // Changed to MultipartFile for file upload
    private MultipartFile nationalIdPhotoFront;

    private MultipartFile nationalIdPhotoBack;

    // Keep URL fields for storing the file paths after upload
    @Size(max = 255, message = "National ID photo front URL must not exceed 255 characters")
    private String nationalIdPhotoFrontUrl;

    @Size(max = 255, message = "National ID photo back URL must not exceed 255 characters")
    private String nationalIdPhotoBackUrl;
}