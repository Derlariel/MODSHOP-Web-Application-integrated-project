package sit.int204.mobileshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Page;

import java.util.List;
import sit.int204.mobileshop.entities.SaleItem;

public interface SaleItemRepository extends JpaRepository<SaleItem, Integer> {

    List<SaleItem> findAllByOrderByCreatedOnAsc();

    @Query("SELECT s FROM SaleItem s WHERE (:filterBrands IS NULL OR s.brand.name IN :filterBrands) AND (:storageGb IS NULL OR s.storageGb = :storageGb) AND (:lowerPrice IS NULL OR s.price >= :lowerPrice) AND (:upperPrice IS NULL OR s.price <= :upperPrice)")
    Page<SaleItem> findAllFilter(Pageable page, @Param("filterBrands") List<String> filterBrands, @Param("storageGb") Integer storageGb, @Param("lowerPrice") Integer lowerPrice, @Param("upperPrice") Integer upperPrice);


}
