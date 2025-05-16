package sit.int204.mobileshop.dtos;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BrandInfoDto {
    private Integer id;

    private String name;

    private String websiteUrl;

    private String countryOfOrigin;

    private Boolean isActive; 
    private Integer noOfSaleItems = 0;
    
    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive != null ? isActive : true;
    }
}
