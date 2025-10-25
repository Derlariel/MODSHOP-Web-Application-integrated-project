package sit.int204.mobileshop.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import sit.int204.mobileshop.OrderStatus;
import sit.int204.mobileshop.entities.Order;
import sit.int204.mobileshop.entities.User;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Page<Order> findAllByUser(User user, Pageable pageable);

    Page<Order> findAllByUserAndOrderStatus(User user, OrderStatus orderStatus, Pageable pageable);

    Page<Order> findAllBySellerId(Long sellerId, Pageable pageable);

    Page<Order> findAllBySellerIdAndOrderStatus(Long seller_id, OrderStatus orderStatus, Pageable pageable);

    Page<Order> findAllBySellerIdAndOrderStatusNot(Long sellerId, OrderStatus orderStatus, Pageable pageable);

    // Get distinct seller nicknames for a specific buyer's orders
    @Query("SELECT DISTINCT s.nickName FROM Order o " +
           "JOIN o.orderItems oi " +
           "JOIN oi.saleItem si " +
           "JOIN si.seller s " +
           "WHERE o.user.id = :userId " +
           "AND s.nickName IS NOT NULL " +
           "ORDER BY s.nickName")
    List<String> findDistinctSellerNamesByUserId(@Param("userId") Long userId);

    // Get distinct buyer nicknames for a specific seller's orders
    @Query("SELECT DISTINCT u.nickName FROM Order o " +
           "JOIN o.user u " +
           "JOIN o.orderItems oi " +
           "JOIN oi.saleItem si " +
           "WHERE si.seller.id = :sellerId " +
           "AND u.nickName IS NOT NULL " +
           "ORDER BY u.nickName")
    List<String> findDistinctBuyerNamesBySellerId(@Param("sellerId") Long sellerId);
}
