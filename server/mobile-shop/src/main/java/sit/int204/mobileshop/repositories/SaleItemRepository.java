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

    @Query("SELECT s FROM SaleItem s WHERE " +
            "(:filterBrands IS NULL OR s.brand.name IN :filterBrands) AND " +
            "(:storageGb IS NULL OR s.storageGb IN :storageGb) AND (" +
            "  (:lowerPrice IS NULL AND :upperPrice IS NULL) OR " +
            "  (:lowerPrice IS NOT NULL AND :upperPrice IS NULL AND :isExactPrice = true AND s.price = :lowerPrice) OR " +
            "  (:lowerPrice IS NOT NULL AND :upperPrice IS NULL AND :isExactPrice = false AND s.price >= :lowerPrice) OR " +
            "  (:lowerPrice IS NOT NULL AND :upperPrice IS NOT NULL AND s.price BETWEEN :lowerPrice AND :upperPrice)" +
            ")")
    Page<SaleItem> findAllFilter(
            Pageable page,
            @Param("filterBrands") List<String> filterBrands,
            @Param("storageGb") List<String> storageGb,
            @Param("lowerPrice") Integer lowerPrice,
            @Param("upperPrice") Integer upperPrice,
            @Param("isExactPrice") Boolean isExactPrice);

}
