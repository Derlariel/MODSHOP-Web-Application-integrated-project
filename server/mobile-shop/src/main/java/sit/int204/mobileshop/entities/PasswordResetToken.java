package sit.int204.mobileshop.entities;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

@Entity
@Data
@Table(name = "password_reset_tokens")
public class PasswordResetToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false , unique = true)
    private String token;

    @Column(name="expiry_time",nullable = false)
    private Instant expiration;

    @Column(name = "is_used",nullable = false )
    private boolean used = false;

}
