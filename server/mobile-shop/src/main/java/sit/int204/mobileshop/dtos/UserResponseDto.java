package sit.int204.mobileshop.dtos;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UserResponseDto {
    private Long id;
    private String email;
    private String fullName;
    private String userType;
    private String nickName;
    
    // Seller fields
    private String phoneNumber;
    private String bankName;
    private String bankAccount;
}