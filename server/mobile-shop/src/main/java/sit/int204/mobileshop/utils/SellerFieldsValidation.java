package sit.int204.mobileshop.utils;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = SellerFieldsValidator.class)
@Documented
public @interface SellerFieldsValidation {
    String message() default "Missing required fields for seller";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
