package sit.int204.mobileshop.dtos;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SaleItemImageDto {
    private Integer id;
    private String fileName;
    private Integer imageViewOrder;


}