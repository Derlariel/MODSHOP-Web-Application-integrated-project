package sit.int204.mobileshop.dtos;

import lombok.Data;
import sit.int204.mobileshop.dtos.BrandDto;

@Data
public class SaleItemDetailForCreateOrUpdateDto {
    private  String model;
    private String description;
    private Integer price;
    private Integer ramGb;
    private Double screenSizeInch;
    private Integer quantity;
    private Integer storageGb;
    private String color;
    private BrandDto brand;
}