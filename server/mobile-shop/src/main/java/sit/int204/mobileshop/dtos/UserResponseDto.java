package sit.int204.mobileshop.dtos;


import lombok.Data;
import lombok.Builder;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class UserResponseDto {
    private Long id;
    private String nickname;
    private String email;
    private String fullName;
    private String phoneNumber;
    private Boolean isActive;
    private String userType;
}