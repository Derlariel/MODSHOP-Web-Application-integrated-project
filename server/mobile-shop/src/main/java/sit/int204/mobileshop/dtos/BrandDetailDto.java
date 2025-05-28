package sit.int204.mobileshop.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.Instant;

@Schema(description = "Complete brand details with timestamps")
@Data
public class BrandDetailDto {
    @Schema(description = "Unique identifier of the brand", example = "1")
    private Integer id;
    
    @Schema(description = "Brand name", example = "Apple")
    private String name;
    
    @Schema(description = "Official website URL", example = "https://www.apple.com")
    private String websiteUrl;
    
    @Schema(description = "Country where the brand originated", example = "United States")
    private String countryOfOrigin;
    
    @Schema(description = "Whether the brand is active", example = "true")
    private Boolean isActive;
    
    @Schema(description = "Brand creation timestamp", example = "2023-01-15T10:30:00Z")
    private Instant createdOn;
    
    @Schema(description = "Last update timestamp", example = "2023-03-20T14:45:00Z")
    private Instant updatedOn;
    
    @Schema(description = "Number of products associated with this brand", example = "12")
    private Integer noOfSaleItems;
}
