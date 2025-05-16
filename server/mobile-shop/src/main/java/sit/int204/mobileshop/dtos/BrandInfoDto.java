package sit.int204.mobileshop.dtos;


import lombok.Data;
import lombok.Builder.Default;



@Data
public class BrandInfoDto {
    private Integer id;
    private String name;
    private String websiteUrl;
    private String countryOfOrigin;
    private Boolean isActive = true;
    private Integer noOfSaleItems = 0;
}
