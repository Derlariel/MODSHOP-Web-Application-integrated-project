package sit.int204.mobileshop.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;


@Getter
@Setter

public class SaleItemDto { 
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
