package sit.int204.mobileshop.dtos;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import sit.int204.mobileshop.entities.User;

import java.time.Instant;
import java.util.List;

@Data
public class OrderResponseDto {
    Long id;
    Long buyerId;
    Long seller;
    Instant orderDate;
    String shippingAddress;
    String orderNote;
    List<OrderItemDto> orderItems;
    String orderStatus;

    @JsonIgnore
    private User user;

    public Long getBuyerId() {
        return user.getId();
    }

}
