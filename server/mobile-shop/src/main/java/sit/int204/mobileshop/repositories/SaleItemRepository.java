package sit.int204.mobileshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;


import java.util.List;
import sit.int204.mobileshop.entities.SaleItem;

public interface SaleItemRepository extends JpaRepository<SaleItem, Integer> {
    
    
    List<SaleItem> findAllByOrderByCreatedOnAsc();

    


    @Query("SELECT s FROM SaleItem s JOIN s.brand b ORDER BY CASE WHEN b.name = UPPER(b.name) THEN 0 ELSE 1 END, b.name ASC")
    List<SaleItem> findAllOrderByBrandNameAsc();

    @Query("SELECT s FROM SaleItem s JOIN s.brand b ORDER BY CASE WHEN b.name = UPPER(b.name) THEN 0 ELSE 1 END, b.name DESC")
    List<SaleItem> findAllOrderByBrandNameDesc();
}
