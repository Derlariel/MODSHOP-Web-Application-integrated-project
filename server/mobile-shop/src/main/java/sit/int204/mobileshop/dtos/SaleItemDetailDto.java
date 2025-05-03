package sit.int204.mobileshop.dtos;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SaleItemDetailDto<T> {
    private Integer id;
    private String model;
    private String brandName;
    private String description;
    @Min(value = 0)
    private Integer price;
    @Min(value = 0)
    private Integer ramGb;
    private BigDecimal screenSizeInch;
    @Min(value = 1)
    private Integer quantity;
    @Min(value = 0)
    private Integer storageGb;
    private String color;
}
