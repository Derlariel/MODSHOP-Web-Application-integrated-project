package sit.int204.mobileshop.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import lombok.Data;
import sit.int204.mobileshop.entities.SaleItem;

@Data
@JsonPropertyOrder({ "no", "saleItemId", "price", "quantity", "description" })
public class OrderItemDto {
    @JsonIgnore
    private Integer id;

    public Integer getNo() {
        return id;
    }
    private Long saleItemId;
    private Integer price;
    private Integer quantity;

    public String getDescription() {
        return saleItem.getDescription();
    }

    @JsonIgnore
    private SaleItem saleItem;






}
