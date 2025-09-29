package sit.int204.mobileshop.repositories.Orders;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int204.mobileshop.entities.OrderItem;

public interface OrderItemRepository extends JpaRepository<OrderItem,Long> {

}
