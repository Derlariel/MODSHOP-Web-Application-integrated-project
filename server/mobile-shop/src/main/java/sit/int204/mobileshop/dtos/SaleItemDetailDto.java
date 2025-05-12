package sit.int204.mobileshop.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter

public class SaleItemDetailDto {
    private Integer id;
    private String model;
    private String brandName;
    private String description;
    @Min(value = 0)
    private Integer price;
    @Min(value = 0)
    private Integer ramGb;
    private BigDecimal screenSizeInch;
    @Min(value = 12, message = "Quantity must be at least 12")
    @Min(value = 0)
    private Integer storageGb;
    private String color;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Bangkok")
    private Instant createdOn;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ssXXX", timezone = "Asia/Bangkok")
    private Instant updatedOn;



}
