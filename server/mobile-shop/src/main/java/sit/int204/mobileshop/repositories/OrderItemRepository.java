package sit.int204.mobileshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int204.mobileshop.entities.OrderItem;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, Integer> {
    boolean existsBySaleItem_Id(Integer saleItemId);
}
