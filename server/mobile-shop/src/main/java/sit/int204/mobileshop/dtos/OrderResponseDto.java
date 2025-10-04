package sit.int204.mobileshop.dtos;

import java.time.Instant;
import java.util.List;

import lombok.Data;

@Data
public class OrderResponseDto {
    private Integer id;
    private Integer buyerId;
    private SellerDto seller;
    private Instant paymentDate;
    private Instant orderDate;
    private String shippingAddress;
    private String orderNote;
    private String orderStatus;
    private List<OrderItemDto> orderItems;

}
