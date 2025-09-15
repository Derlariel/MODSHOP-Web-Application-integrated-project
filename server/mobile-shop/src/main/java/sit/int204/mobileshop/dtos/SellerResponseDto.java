package sit.int204.mobileshop.dtos;

import lombok.Data;

@Data
public class SellerResponseDto {
    private Long id;
    private String email;
    private String fullName;
    private String userType;
    private String phoneNumber;
    private String bankName;
    private String bankAccount;
    private String nickName;
}