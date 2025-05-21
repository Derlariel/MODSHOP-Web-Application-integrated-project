package sit.int204.mobileshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import sit.int204.mobileshop.entities.SaleItem;

public interface SaleItemRepository extends JpaRepository<SaleItem, Integer> {
    List<SaleItem> findAllByOrderByCreatedOnAsc();

    @Query("SELECT s FROM SaleItem s JOIN s.brand b ORDER BY (b.name = UPPER(b.name)) DESC, b.name ASC")
    List<SaleItem> findAllOrderByBrandNameAsc();

    @Query("SELECT s FROM SaleItem s JOIN s.brand b ORDER BY (b.name = UPPER(b.name)) DESC, b.name DESC")
    List<SaleItem> findAllOrderByBrandNameDesc();
}
