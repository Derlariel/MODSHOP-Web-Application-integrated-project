package sit.int204.mobileshop.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Schema(description = "Detailed information about a sale item")
@Getter
@Setter
public class SaleItemDetailDto {
    @Schema(description = "Unique identifier of the product", example = "1")
    private Integer id;
    
    @Schema(description = "Model name of the product", example = "iPhone 15")
    private String model;
    
    @Schema(description = "Brand name of the product", example = "Apple")
    private String brandName;
    
    @Schema(description = "Detailed product description", 
            example = "The iPhone 15 features a stunning display, powerful A16 Bionic chip, and incredible camera system.")
    private String description;
    
    @Schema(description = "Quantity available in stock", example = "10")
    private Integer quantity;
    
    @Schema(description = "Price in THB", example = "35900")
    @Min(value = 0)
    private Integer price;
    
    @Schema(description = "RAM capacity in GB", example = "8")
    @Min(value = 0)
    private Integer ramGb;
    
    @Schema(description = "Screen size in inches", example = "6.1")
    private BigDecimal screenSizeInch;
    
    @Schema(description = "Storage capacity in GB", example = "128")
    @Min(value = 0)
    private Integer storageGb;
    
    @Schema(description = "Color of the product", example = "Midnight Black")
    private String color;
    
    @Schema(description = "Creation timestamp", example = "2023-05-15T10:30:00+07:00")
    private Instant createdOn;
    
    @Schema(description = "Last update timestamp", example = "2023-05-16T14:20:00+07:00")
    private Instant updatedOn;
}