package sit.int204.mobileshop.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "buyer_id", nullable = false)
    private Long buyerId;

    @Column(name = "seller_id" , nullable = false)
    private Long sellerId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Status status = Status.PENDING;

    @Column(name = "total_price", nullable = false)
    private Integer totalPrice = 0;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(name = "order_date" , nullable = false)
    private LocalDateTime orderDate = LocalDateTime.now();

    @Column(name = "shipping_address" )
    private String shippingAddress;

    @Column(name = "order_note")
    private String orderNote;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(name = "created_on", nullable = false, updatable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm")
    @Column(name = "updated_on", nullable = false, updatable = false)
    private LocalDateTime updatedOn = LocalDateTime.now();

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> items;

    public enum Status {
        PENDING, PAID, SHIPPED, CANCELLED , COMPLETED
    }
}
