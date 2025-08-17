package sit.int204.mobileshop.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Immutable;

/**
 * Mapping for DB view
 */
@Getter
@Setter
@Entity
@Immutable
@Table(name = "distinct_storage_size")
public class DistinctStorageSize {

    @Id
    @Column(name = "storage_gb")
    private Integer storageGb;

}