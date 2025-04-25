package sit.int204.mobileshopspike.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "products")
public class Product {
    @Id
    @Column(name = "id", nullable = false)
    private Integer id;

    @Size(max = 60)
    @NotNull
    @Column(name = "name", nullable = false, length = 60)
    private String name;

    @Size(max = 400)
    @Column(name = "description", length = 400)
    private String description;

    @NotNull
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @NotNull
    @Column(name = "price", nullable = false)
    private Integer price;

    @Size(max = 50)
    @NotNull
    @Column(name = "brand", nullable = false, length = 50)
    private String brand;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "createdOn", nullable = false)
    private Instant createdOn;

    @NotNull
    @ColumnDefault("CURRENT_TIMESTAMP")
    @Column(name = "updatedOn", nullable = false)
    private Instant updatedOn;

}