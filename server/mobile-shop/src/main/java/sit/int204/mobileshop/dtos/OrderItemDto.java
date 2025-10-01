package sit.int204.mobileshop.dtos;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import lombok.Data;

@Data
@JsonPropertyOrder({ "no", "saleItemId", "price", "quantity", "description" })
public class OrderItemDto {
    private Integer no;
    private Integer saleItemId;
    private Integer price;
    private Integer quantity;
    private String description;   
}
