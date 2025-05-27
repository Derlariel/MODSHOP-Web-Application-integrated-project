package sit.int204.mobileshop.repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import sit.int204.mobileshop.entities.Brand;



public interface BrandRepository extends JpaRepository<Brand, Integer> {
    boolean existsByNameIgnoreCase(String name);
}
