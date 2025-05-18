package sit.int204.mobileshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import sit.int204.mobileshop.entities.SaleItem;

public interface SaleItemRepository extends JpaRepository<SaleItem, Integer> {
    List<SaleItem> findAllByOrderByCreatedOnAsc();
}
