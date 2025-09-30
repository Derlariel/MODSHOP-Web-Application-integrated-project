package sit.int204.mobileshop.dtos.Order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import sit.int204.mobileshop.entities.Order;
import sit.int204.mobileshop.entities.OrderItem;

import java.util.List;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderResponseDto {
    private Long orderId;
    private Long buyerId;
    private Long sellerId;
    private String status;
    private Integer totalPrice;
    private String shippingAddress;
    private String orderNote;
    private String orderDate;
    private List<OrderItemDto> items;

    public OrderResponseDto(Order order, List<OrderItem> orderItems) {
        this.orderId = order.getId();
        this.buyerId = order.getBuyerId();
        this.sellerId = order.getSellerId();
        this.status = order.getStatus().name();
        this.totalPrice = order.getTotalPrice();
        this.shippingAddress = order.getShippingAddress();
        this.orderNote = order.getOrderNote();
        this.orderDate = order.getOrderDate().toString();

        this.items = orderItems.stream()
                .map(oi -> new OrderItemDto(
                        oi.getSaleItem().getId(),
                        oi.getQuantity(),
                        oi.getDescription(),
                        oi.getNo()
                ))
                .collect(Collectors.toList());
    }
}
