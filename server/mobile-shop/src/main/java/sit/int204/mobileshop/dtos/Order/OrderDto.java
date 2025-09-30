package sit.int204.mobileshop.dtos.Order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private Long buyerId;
    private Long sellerId;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItemDto> items;

}
