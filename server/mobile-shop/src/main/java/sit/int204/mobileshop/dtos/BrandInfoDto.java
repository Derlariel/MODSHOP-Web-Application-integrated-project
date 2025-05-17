package sit.int204.mobileshop.dtos;


<<<<<<< HEAD
=======
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
>>>>>>> pre-dev
import lombok.Data;

@Data
public class BrandInfoDto {
    private Integer id;

    private String name;

    private String websiteUrl;

    private String countryOfOrigin;

<<<<<<< HEAD
    private Boolean isActive;
    private Integer noOfSaleItems = 0;

=======
    private Boolean isActive; 
    private Integer noOfSaleItems = 0;
    
>>>>>>> pre-dev
    public void setName(String name) {
        this.name = name != null ? name.trim() : null;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive != null ? isActive : true;
    }
<<<<<<< HEAD
}
=======
}
>>>>>>> pre-dev
