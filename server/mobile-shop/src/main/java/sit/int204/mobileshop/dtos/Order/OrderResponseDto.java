package sit.int204.mobileshop.dtos.Order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.int204.mobileshop.entities.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private Long buyerId;
    private String status;
    private List<OrderItemDto> items;

    public OrderResponseDto(Long orderId, Long buyerId, String status, List<OrderItem> orderItems) {
        this.orderId = orderId;
        this.buyerId = buyerId;
        this.status = status;
        this.items = orderItems.stream()
                .map(oi -> new OrderItemDto(
                        oi.getSaleItem().getId(),
                        oi.getQuantity()
                ))
                .collect(Collectors.toList());
    }
}