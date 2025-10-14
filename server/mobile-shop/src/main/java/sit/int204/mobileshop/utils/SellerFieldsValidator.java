package sit.int204.mobileshop.utils;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.web.multipart.MultipartFile;
import sit.int204.mobileshop.dtos.RegisterUserDto;

public class SellerFieldsValidator implements ConstraintValidator<SellerFieldsValidation, RegisterUserDto> {

    @Override
    public boolean isValid(RegisterUserDto dto, ConstraintValidatorContext context) {
        if (!"SELLER".equalsIgnoreCase(dto.getRole())) {
            return true; // Not a seller, no additional validation needed
        }

        boolean isValid = true;
        context.disableDefaultConstraintViolation();

        // Validate required seller fields
        if (isEmpty(dto.getMobileNumber())) {
            context.buildConstraintViolationWithTemplate("Mobile number is required for sellers")
                    .addPropertyNode("mobileNumber")
                    .addConstraintViolation();
            isValid = false;
        }

        if (isEmpty(dto.getBankAccountNumber())) {
            context.buildConstraintViolationWithTemplate("Bank account number is required for sellers")
                    .addPropertyNode("bankAccountNumber")
                    .addConstraintViolation();
            isValid = false;
        }

        if (isEmpty(dto.getBankName())) {
            context.buildConstraintViolationWithTemplate("Bank name is required for sellers")
                    .addPropertyNode("bankName")
                    .addConstraintViolation();
            isValid = false;
        }

        if (isEmpty(dto.getNationalIdNumber())) {
            context.buildConstraintViolationWithTemplate("National ID number is required for sellers")
                    .addPropertyNode("nationalIdNumber")
                    .addConstraintViolation();
            isValid = false;
        }

        // National ID must be exactly 13 digits
        if (!isEmpty(dto.getNationalIdNumber()) && !dto.getNationalIdNumber().matches("^\\d{13}$")) {
            context.buildConstraintViolationWithTemplate("National ID number must be exactly 13 digits")
                    .addPropertyNode("nationalIdNumber")
                    .addConstraintViolation();
            isValid = false;
        }

    // Mobile number: allow optional '+', digits, spaces, and dashes; total length 6-20 and must start/end with a digit
    if (!isEmpty(dto.getMobileNumber()) && !dto.getMobileNumber().matches("^[+]?\\d[\\d \\-]{4,18}\\d$")) {
            context.buildConstraintViolationWithTemplate("Mobile number format is invalid (allowed digits, optional '+', spaces or '-')")
                    .addPropertyNode("mobileNumber")
                    .addConstraintViolation();
            isValid = false;
        }

        // Bank account: digits only 3-30 (updated requirement)
        if (!isEmpty(dto.getBankAccountNumber()) && !dto.getBankAccountNumber().matches("^\\d{3,30}$")) {
            context.buildConstraintViolationWithTemplate("Bank account number must be 3-30 digits")
                    .addPropertyNode("bankAccountNumber")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate national ID photos
        if (!isValidFile(dto.getNationalIdPhotoFront())) {
            context.buildConstraintViolationWithTemplate("National ID photo (front) is required for sellers")
                    .addPropertyNode("nationalIdPhotoFront")
                    .addConstraintViolation();
            isValid = false;
        }

        if (!isValidFile(dto.getNationalIdPhotoBack())) {
            context.buildConstraintViolationWithTemplate("National ID photo (back) is required for sellers")
                    .addPropertyNode("nationalIdPhotoBack")
                    .addConstraintViolation();
            isValid = false;
        }

        // Validate file types and sizes
        if (dto.getNationalIdPhotoFront() != null && !dto.getNationalIdPhotoFront().isEmpty()) {
            if (!isValidImageFile(dto.getNationalIdPhotoFront())) {
                context.buildConstraintViolationWithTemplate("National ID photo (front) must be a valid image file")
                        .addPropertyNode("nationalIdPhotoFront")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        if (dto.getNationalIdPhotoBack() != null && !dto.getNationalIdPhotoBack().isEmpty()) {
            if (!isValidImageFile(dto.getNationalIdPhotoBack())) {
                context.buildConstraintViolationWithTemplate("National ID photo (back) must be a valid image file")
                        .addPropertyNode("nationalIdPhotoBack")
                        .addConstraintViolation();
                isValid = false;
            }
        }

        return isValid;
    }

    private boolean isEmpty(String value) {
        return value == null || value.trim().isEmpty();
    }

    private boolean isValidFile(MultipartFile file) {
        return file != null && !file.isEmpty();
    }

    private boolean isValidImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return false;
        }

        // Check file size (5MB limit)
        return file.getSize() <= 5 * 1024 * 1024;
    }
}