package sit.int204.mobileshopspike.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int204.mobileshopspike.entities.Product;

public interface ProductRepository extends JpaRepository<Product, Integer> {
}
