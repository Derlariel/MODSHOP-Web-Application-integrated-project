package sit.int204.mobileshop.dtos;

import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

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
    private BigDecimal screenSizeInch;
    @Min(value = 1)
    private Integer quantity;
    private String color;
    private Instant createdOn;
    private Instant updatedOn;
    private BigDecimal rate;
}
