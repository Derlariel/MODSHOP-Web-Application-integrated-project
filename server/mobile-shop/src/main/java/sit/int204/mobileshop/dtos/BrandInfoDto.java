package sit.int204.mobileshop.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Schema(description = "Detailed brand information")
@Data
public class BrandInfoDto {
    @Schema(description = "Unique identifier of the brand", example = "1")
    private Integer id;

    @Schema(description = "Brand name", example = "Apple")
    private String name;

    @Schema(description = "Official website URL", example = "https://www.apple.com")
    private String websiteUrl;

    @Schema(description = "Country where the brand originated", example = "United States")
    private String countryOfOrigin;

    @Schema(description = "Whether the brand is active in the system", example = "true", defaultValue = "true")
    private Boolean isActive;
    
    @Schema(description = "Number of products associated with this brand", example = "12")
    private Integer noOfSaleItems = 0;

    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive != null ? isActive : true;
    }
}


