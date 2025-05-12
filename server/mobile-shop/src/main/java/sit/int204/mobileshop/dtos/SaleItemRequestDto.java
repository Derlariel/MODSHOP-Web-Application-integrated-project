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



    @NotBlank
    private String model;

    @NotNull
    private BrandDto brand;

    @NotBlank
    private String description;

    @NotNull
    @Min(0)
    private Integer price;

    private Integer ramGb;
    private BigDecimal screenSizeInch;

    @NotNull
    @Min(1)
    private Integer quantity;

    @Min(value = 12, message = "storageGb must be at least 12")
    private Integer storageGb;
    private String color;
}

