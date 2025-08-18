package sit.int204.mobileshop.dtos;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class SaleItemImageRequest {
    private Integer order;
    private String fileName;
    private String status;
    private MultipartFile imageFile;


}
