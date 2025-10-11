package sit.int204.mobileshop.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import sit.int204.mobileshop.OrderStatus;
import sit.int204.mobileshop.entities.Order;
import sit.int204.mobileshop.entities.User;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findAllByUser(User user, Pageable pageable);

    Page<Order> findAllByUserAndOrderStatus(User user, OrderStatus orderStatus, Pageable pageable);

    Page<Order> findAllBySellerId(Long sellerId, Pageable pageable);

    Page<Order> findAllBySellerIdAndOrderStatus(Long seller_id, OrderStatus orderStatus, Pageable pageable);
}
