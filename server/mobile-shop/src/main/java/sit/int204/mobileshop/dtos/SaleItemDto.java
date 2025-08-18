package sit.int204.mobileshop.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Sale Item summary information")
@Getter
@Setter
public class SaleItemDto { 
    @Schema(description = "Unique identifier of the product", example = "1")
    private Integer id;
    
    @Schema(description = "Model name of the product", example = "iPhone 15")
    private String model;
    
    @Schema(description = "Brand name of the product", example = "Apple")
    private String brandName;
    
    @Schema(description = "Price in THB", example = "35900")
    @Min(value = 0)
    private Integer price;
    
    @Schema(description = "Storage capacity in GB", example = "128")
    @Min(value = 0)
    private Integer storageGb;
    
    @Schema(description = "RAM capacity in GB", example = "8")
    @Min(value = 0)
    private Integer ramGb;
    
    @Schema(description = "Color of the product", example = "Midnight Black")
    private String color;

    private String image;
}
