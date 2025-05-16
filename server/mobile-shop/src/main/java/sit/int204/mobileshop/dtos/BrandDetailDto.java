package sit.int204.mobileshop.dtos;
import lombok.Data;

import java.time.Instant;

@Data
public class BrandDetailDto {
    private Integer id;
    private String name;
    private String websiteUrl;
    private String countryOfOrigin;
    private Boolean isActive;
    private Instant createdOn;
    private Instant updatedOn;
    private Integer noOfSaleItems;
}
