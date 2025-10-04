package sit.int204.mobileshop.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({ "no", "saleItemId", "price", "quantity", "description" })
public class OrderItemDto {
    private Integer no;
    private Long saleItemId;
    private Integer price;
    private Integer quantity;
    private String description;

    private String image;
    private String brandName;
    private String model;
    private String color;
    private Integer storageGb;
    private Integer lineTotal;
}
