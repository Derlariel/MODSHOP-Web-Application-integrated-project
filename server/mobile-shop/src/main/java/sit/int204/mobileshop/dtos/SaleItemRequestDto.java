package sit.int204.mobileshop.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
@Getter @Setter
public class SaleItemRequestDto {

    @Size(min = 12)
    private String name;

    private String model;

    private BrandDto brand;

    private String description;

    @Min(0)
    private Integer price;

    private Integer ramGb;
    private BigDecimal screenSizeInch;

    private Integer quantity;

    private Integer storageGb;
    private String color;
}

