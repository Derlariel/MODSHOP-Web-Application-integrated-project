package sit.int204.mobileshop.repositories;
import org.springframework.data.jpa.repository.JpaRepository;

import sit.int204.mobileshop.dtos.BrandDetailDto;
import sit.int204.mobileshop.entities.Brand;
import java.util.Optional;
import java.util.List;


public interface BrandRepository extends JpaRepository<Brand, Integer> {
    boolean existsByNameIgnoreCase(String name);
}
