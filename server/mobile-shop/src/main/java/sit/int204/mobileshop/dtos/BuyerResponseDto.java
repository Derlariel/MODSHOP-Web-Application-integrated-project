package sit.int204.mobileshop.dtos;

import lombok.Data;

@Data
public class BuyerResponseDto {
    private Long id;
    private String email;
    private String fullName;
    private String userType;
    private String nickName;
}