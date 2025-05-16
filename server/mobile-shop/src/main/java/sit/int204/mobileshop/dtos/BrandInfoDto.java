package sit.int204.mobileshop.dtos;


import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandInfoDto {
    private Integer id;

    @Size
    @Size(max = 30, message = "Brand name must not exceed 30 characters")
    private String name;

    @Size(max = 40, message = "Website URL must not exceed 40 characters")
    private String websiteUrl;

    @Size(max = 80, message = "Country of origin must not exceed 80 characters")
    private String countryOfOrigin;

    private Boolean isActive = true; 
    private Integer noOfSaleItems = 0; 
}
