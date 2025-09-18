package sit.int204.mobileshop.dtos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateProfileDto {
    @NotNull(message = "Nickname is required")
    @NotBlank(message = "Nickname cannot be blank")
    @Size(max = 30, message = "Nickname must not exceed 30 characters")
    private String nickName;

    @NotNull(message = "Full name is required")
    @NotBlank(message = "Full name cannot be blank")
    @Size(max = 50, message = "Full name must not exceed 50 characters")
    private String fullName;
    
    // Seller-specific fields (optional)
    private String phoneNumber;
    private String bankName;
    private String bankAccount;
    private String nationalIdNumber;
}
