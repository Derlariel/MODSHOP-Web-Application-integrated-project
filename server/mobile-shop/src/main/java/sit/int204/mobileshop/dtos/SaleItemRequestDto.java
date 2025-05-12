package sit.int204.mobileshop.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class SaleItemRequestDto {
    @NotBlank
    private String model;

    @NotNull
    private BrandDto brand;

    private String description;

    @NotNull
    @Min(0)
    private Integer price;

    private Integer ramGb;
    private BigDecimal screenSizeInch;

    @NotNull
    @Min(0)
    private Integer quantity;

    private Integer storageGb;
    private String color;

    @Getter
    @Setter
    public static class BrandDto {
        @NotNull
        private Integer id;
        private String name;
    }
}