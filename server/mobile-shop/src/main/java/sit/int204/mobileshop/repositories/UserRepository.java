package sit.int204.mobileshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import sit.int204.mobileshop.entities.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

}
