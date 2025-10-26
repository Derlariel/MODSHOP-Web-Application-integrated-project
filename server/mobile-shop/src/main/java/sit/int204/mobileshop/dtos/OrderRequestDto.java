package sit.int204.mobileshop.dtos;

import com.fasterxml.jackson.annotation.JsonAlias;
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

    @JsonAlias({"orderItems", "items"})
    private List<OrderItemDto> items;
}
