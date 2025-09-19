package sit.int204.mobileshop.entities;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;

@Getter
@Setter
@Entity
@Table(name = "sellers")
@PrimaryKeyJoinColumn(name = "user_id")
public class Seller extends User {
    
    @Size(max = 20)
    @NotNull
    @Column(name = "mobile_number", nullable = false, length = 20)
    private String mobileNumber;

    @Size(max = 50)
    @NotNull
    @Column(name = "bank_account_number", nullable = false, length = 50)
    private String bankAccountNumber;

    @Size(max = 100)
    @NotNull
    @Column(name = "bank_name", nullable = false, length = 100)
    private String bankName;

    @Size(max = 50)
    @NotNull
    @Column(name = "national_id_number", nullable = false, length = 50)
    private String nationalIdNumber;

    @Size(max = 255)
    @NotNull
    @Column(name = "national_id_photo_front", nullable = false)
    private String nationalIdPhotoFront;

    @Size(max = 255)
    @NotNull
    @Column(name = "national_id_photo_back", nullable = false)
    private String nationalIdPhotoBack;

}