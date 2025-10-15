package sit.int204.mobileshop.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import sit.int204.mobileshop.entities.SaleItem;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface SaleItemRepository extends JpaRepository<SaleItem, Integer>, JpaSpecificationExecutor<SaleItem> {

    Page<SaleItem> findSaleItemsBySellerId(Long seller_id, Pageable pageable);

    List<SaleItem> findAllByOrderByCreatedOnAsc();

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from SaleItem s where s.id = :id")
    Optional<SaleItem> findByIdForUpdate(@Param("id") Integer id);

//    @Query("SELECT s FROM SaleItem s WHERE " +
//            "(:filterBrands IS NULL OR s.brand.name IN :filterBrands) AND " +
//            "(" +
//            "  :storageGb IS NULL AND :includeNullStorage = false OR " +
//            "  (:storageGb IS NOT NULL AND s.storageGb IN :storageGb AND :includeNullStorage = false) OR " +
//            "  (:includeNullStorage = true AND s.storageGb IS NULL) OR " +
//            "  (:storageGb IS NOT NULL AND :includeNullStorage = true AND s.storageGb IN :storageGb)" +
//            ") AND (" +
//            "  (:lowerPrice IS NULL AND :upperPrice IS NULL) OR " +
//            "  (:lowerPrice IS NOT NULL AND :upperPrice IS NULL AND :isExactPrice = true AND s.price = :lowerPrice) OR " +
//            "  (:lowerPrice IS NOT NULL AND :upperPrice IS NOT NULL AND s.price BETWEEN :lowerPrice AND :upperPrice)" +
//            ")")
//    Page<SaleItem> findAllFilter(
//            Pageable page,
//            @Param("filterBrands") List<String> filterBrands,
//            @Param("storageGb") List<String> storageGb,
//            @Param("includeNullStorage") Boolean includeNullStorage,
//            @Param("lowerPrice") Integer lowerPrice,
//            @Param("upperPrice") Integer upperPrice,
//            @Param("isExactPrice") Boolean isExactPrice);
//    @Query("SELECT s FROM SaleItem s WHERE (:filterBrands IS NULL OR s.brand.name IN :filterBrands) AND (:storageGb IS NULL OR s.storageGb IN :storageGb ) AND (:lowerPrice IS NULL OR s.price >= :lowerPrice) AND (:upperPrice IS NULL OR s.price <= :upperPrice)")
//    Page<SaleItem> findAllFilter(Pageable page, @Param("filterBrands") List<String> filterBrands, @Param("storageGb")List<String> storageGb, @Param("lowerPrice") Integer lowerPrice, @Param("upperPrice") Integer upperPrice);
//
//
//
//
//    @Query("SELECT s FROM SaleItem s WHERE " +
//            "(:filterBrands IS NULL OR s.brand.name IN :filterBrands) AND " +
//            "(" +
//            "  :storageGb IS NULL AND :includeNullStorage = false OR " +
//            "  (:storageGb IS NOT NULL AND s.storageGb IN :storageGb AND :includeNullStorage = false) OR " +
//            "  (:includeNullStorage = true AND s.storageGb IS NULL) OR " +
//            "  (:storageGb IS NOT NULL AND :includeNullStorage = true AND s.storageGb IN :storageGb)" +
//            ") AND (" +
//            "  (:lowerPrice IS NULL AND :upperPrice IS NULL) OR " +
//            "  (:lowerPrice IS NOT NULL AND :upperPrice IS NULL AND :isExactPrice = true AND s.price = :lowerPrice) OR " +
//            "  (:lowerPrice IS NOT NULL AND :upperPrice IS NOT NULL AND s.price BETWEEN :lowerPrice AND :upperPrice)" +
//            ") AND (" +
//            "  :q IS NULL OR :q = '' OR " +
//            "  LOWER(s.description) LIKE LOWER(CONCAT('%', :q, '%')) OR " +
//            "  LOWER(s.model)       LIKE LOWER(CONCAT('%', :q, '%')) OR " +
//            "  LOWER(s.color)       LIKE LOWER(CONCAT('%', :q, '%')) " +
//            ")")
//    Page<SaleItem> findAllFilterWithKeyword(
//            Pageable page,
//            @Param("filterBrands") List<String> filterBrands,
//            @Param("storageGb") List<String> storageGb,
//            @Param("includeNullStorage") Boolean includeNullStorage,
//            @Param("lowerPrice") Integer lowerPrice,
//            @Param("upperPrice") Integer upperPrice,
//            @Param("isExactPrice") Boolean isExactPrice,
//            @Param("q") String q);

}
