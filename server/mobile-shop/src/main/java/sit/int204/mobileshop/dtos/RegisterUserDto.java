package sit.int204.mobileshop.dtos;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import sit.int204.mobileshop.utils.SellerFieldsValidation;

@Data
@SellerFieldsValidation
public class RegisterUserDto {
    @NotBlank(message = "Nickname is required")
    private String nickname;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 8, message = "Password must be at least 8 characters")
//    @Pattern(
//            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&]).+$",
//            message = "Password must contain upper, lower, number, and special character"
//    )
    private String password;

    @NotBlank(message = "Full name is required")
    @Size(min = 4, max = 40, message = "Full name must be between 4 and 40 characters")
    private String fullname;

    @NotBlank(message = "Role is required")
    private String role;

    private String mobileNumber;
    private String bankAccountNumber;
    private String bankName;
    private String nationalIdNumber;
    private String nationalIdPhotoFront;
    private String nationalIdPhotoBack;
}

