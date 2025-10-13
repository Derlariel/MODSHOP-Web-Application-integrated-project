package sit.int204.mobileshop.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import sit.int204.mobileshop.entities.PasswordResetToken;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
}
