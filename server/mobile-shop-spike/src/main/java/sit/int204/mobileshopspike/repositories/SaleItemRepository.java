package sit.int204.mobileshopspike.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int204.mobileshopspike.entities.SaleItem;

public interface SaleItemRepository extends JpaRepository<SaleItem, Integer> {
}
