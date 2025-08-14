package sit.int204.mobileshop.dtos;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Setter
@AllArgsConstructor
public class SaleItemImageDto {
    @NotNull
    private MultipartFile imageFile;
    private Byte isPrimary;

}