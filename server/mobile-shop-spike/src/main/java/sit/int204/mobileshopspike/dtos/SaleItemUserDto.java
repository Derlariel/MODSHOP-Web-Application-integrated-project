package sit.int204.mobileshopspike.dtos;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;
import sit.int204.mobileshopspike.entities.Brand;

@Getter
@Setter
public class SaleItemUserDto<T> {
    private Integer id;
    private String model;
    private String brandName;
    @Min(value = 0)
    private Integer price;
    @Min(value = 0)
    private Integer storageGb;
    @Min(value = 0)
    private Integer ramGb;
    private String color;
}
