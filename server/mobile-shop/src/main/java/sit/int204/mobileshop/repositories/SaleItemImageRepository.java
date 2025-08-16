package sit.int204.mobileshop.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import sit.int204.mobileshop.entities.SaleItemImage;

@Repository
public interface SaleItemImageRepository extends JpaRepository<SaleItemImage, Integer> {
    List<SaleItemImage> findAllBySaleItemId(Integer saleItemId);

//    @Modifying
//    @Query("UPDATE SaleItemImage i SET i.isPrimary = 0 WHERE i.saleItem.id = :saleItemId AND i.isPrimary = 1")
//    void updateAllNonPrimaryBySaleItemId(@Param("saleItemId") Integer saleItemId);
}
