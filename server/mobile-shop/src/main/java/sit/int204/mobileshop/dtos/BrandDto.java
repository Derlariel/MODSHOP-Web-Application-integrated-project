package sit.int204.mobileshop.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Basic brand information")
@Data
public class BrandDto {
    @Schema(description = "Unique identifier of the brand", example = "1")
    private Integer id;
    
    @Schema(description = "Brand name", example = "Apple")
    private String name;
}
