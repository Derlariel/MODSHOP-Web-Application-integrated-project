package sit.int204.mobileshop.dtos;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Schema(description = "Request object for creating or updating a sale item")
@Getter
@Setter
public class SaleItemRequestDto {
    @Schema(description = "Model name of the product", example = "iPhone 15", required = true)
    @NotBlank
    private String model;


    @Schema(description = "Brand information", required = true)
    @NotNull
    private BrandDto brand;

    @Schema(description = "Detailed product description", 
           example = "The iPhone 15 features a stunning display, powerful A16 Bionic chip, and incredible camera system.", 
           required = true)
    @NotBlank 
    private String description;

    @Schema(description = "Price in THB", example = "35900", required = true, minimum = "0")
    @NotNull
    @Min(0)
    private Integer price;

    @Schema(description = "RAM capacity in GB", example = "8")
    private Integer ramGb;
    
    @Schema(description = "Screen size in inches", example = "6.1")
    private BigDecimal screenSizeInch;

    @Schema(description = "Quantity available in stock", example = "10")
    private Integer quantity;

    @Schema(description = "Storage capacity in GB", example = "128")
    private Integer storageGb;
    
    @Schema(description = "Color of the product", example = "Midnight Black")
    private String color;

    @Schema(description = "Brand information for a sale item")
    @Getter
    @Setter
    public static class BrandDto {
        @Schema(description = "Unique identifier of the brand", example = "1", required = true)
        @NotNull
        private Integer id;
        
        @Schema(description = "Brand name", example = "Apple")
        private String name;
    }
}