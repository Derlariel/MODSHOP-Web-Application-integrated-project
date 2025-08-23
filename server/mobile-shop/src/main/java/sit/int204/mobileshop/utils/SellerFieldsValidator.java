package sit.int204.mobileshop.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import sit.int204.mobileshop.dtos.RegisterUserDto;

public class SellerFieldsValidator implements ConstraintValidator<SellerFieldsValidation, RegisterUserDto> {

    @Override
    public boolean isValid(RegisterUserDto dto, ConstraintValidatorContext context) {
        if (dto == null) {
            return true;
        }

        if ("SELLER".equalsIgnoreCase(dto.getRole())) {
            boolean valid = true;

            if (dto.getMobileNumber() == null || dto.getMobileNumber().isBlank()) {
                addViolation(context, "mobileNumber is required for seller");
                valid = false;
            }
            if (dto.getBankAccountNumber() == null || dto.getBankAccountNumber().isBlank()) {
                addViolation(context, "bankAccountNumber is required for seller");
                valid = false;
            }
            if (dto.getBankName() == null || dto.getBankName().isBlank()) {
                addViolation(context, "bankName is required for seller");
                valid = false;
            }
            if (dto.getNationalIdNumber() == null || dto.getNationalIdNumber().isBlank()) {
                addViolation(context, "nationalIdNumber is required for seller");
                valid = false;
            }
            if (dto.getNationalIdPhotoFront() == null || dto.getNationalIdPhotoFront().isBlank()) {
                addViolation(context, "nationalIdPhotoFront is required for seller");
                valid = false;
            }
            if (dto.getNationalIdPhotoBack() == null || dto.getNationalIdPhotoBack().isBlank()) {
                addViolation(context, "nationalIdPhotoBack is required for seller");
                valid = false;
            }

            return valid;
        }
        
        return true;
    }

    private void addViolation(ConstraintValidatorContext context, String message) {
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
                .addConstraintViolation();
    }
}
