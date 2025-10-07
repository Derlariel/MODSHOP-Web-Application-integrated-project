package sit.int204.mobileshop.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequestDto {
    private Integer buyerId;
    private Integer sellerId;
    private String shippingAddress;
    private String orderNote;
    private List<OrderItemDto> items;
}
