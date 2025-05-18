package sit.int204.mobileshop.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "sale_item")
public class SaleItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 60)
    @NotNull
    @Column(name = "model", nullable = false, length = 60)
    private String model;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "brand_id", nullable = false)
    private Brand brand;

    @NotNull
    @Lob
    @Column(name = "description", nullable = false)
    private String description;

    @NotNull
    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "ram_gb")
    private Integer ramGb;

    @Column(name = "screen_size_inch", precision = 4, scale = 1)
    private BigDecimal screenSizeInch;

    @Column(name = "storage_gb")
    private Integer storageGb;

    @Size(max = 30)
    @Column(name = "color", length = 30)
    private String color;

    @ColumnDefault("1")
    @Column(name = "quantity", nullable = false)
    private Integer quantity;

    @Column(name = "created_on", insertable = false, updatable = false)
    private Instant createdOn;

    @Column(name = "updated_on", insertable = false, updatable = false)
    private Instant updatedOn;

}

