package sit.int204.mobileshop.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.Instant;
import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_id", nullable = false)
    private Integer id;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "order_date", nullable = false)
    private Instant orderDate;

    @Size(max = 255)
    @NotNull
    @Column(name = "shipping_address", nullable = false)
    private String shippingAddress;

    @Size(max = 255)
    @Column(name = "order_note")
    private String orderNote;

    @ColumnDefault("'COMPLETED'")
    @Lob
    @Column(name = "order_status")
    private String orderStatus;

    @OneToMany(mappedBy = "order")
    private Set<OrderItem> orderItems = new LinkedHashSet<>();

}