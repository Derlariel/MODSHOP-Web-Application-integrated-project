package sit.int204.mobileshop.dtos;


import lombok.Data;



@Data
public class BrandInfoDto {
    private Integer id;
    private String name;
    private String websiteUrl;
    private String countryOfOrigin;
    private Boolean isActive;
    private Integer noOfSaleItems;
}
