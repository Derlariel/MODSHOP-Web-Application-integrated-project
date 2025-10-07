package sit.int204.mobileshop.dtos;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellerDto {
    private Long id;
    private String email;
    private String fullName;
    private String userType;
    private String nickName;
}
