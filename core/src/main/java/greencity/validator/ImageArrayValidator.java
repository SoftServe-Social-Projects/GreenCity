package greencity.validator;

import greencity.annotations.ImageArrayValidation;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.multipart.MultipartFile;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class ImageArrayValidator implements ConstraintValidator<ImageArrayValidation, MultipartFile[]> {
    private List<String> allowedTypes;
    private String messageTemplate;

    @Value("${max-multipart-comment-image-size}")
    private String maxImageSize;

    @Override
    public void initialize(ImageArrayValidation constraintAnnotation) {
        this.allowedTypes = Arrays.asList(constraintAnnotation.allowedTypes());
        this.messageTemplate = constraintAnnotation.message();
    }

    @Override
    public boolean isValid(MultipartFile[] images, ConstraintValidatorContext context) {
        if (Objects.isNull(images) || images.length == 0) {
            return true; // skip validation for null or empty inputs
        }

        boolean isValid = Arrays.stream(images).allMatch(this::isValidImage);

        if (!isValid) {
            addViolation(context);
        }
        return isValid;
    }

    long getMaxSizeInBytes() {
        if (maxImageSize.endsWith("KB")) {
            return Long.parseLong(maxImageSize.replace("KB", "").trim()) * 1024;
        } else if (maxImageSize.endsWith("MB")) {
            return Long.parseLong(maxImageSize.replace("MB", "").trim()) * 1024 * 1024;
        } else if (maxImageSize.endsWith("GB")) {
            return Long.parseLong(maxImageSize.replace("GB", "").trim()) * 1024 * 1024 * 1024;
        } else {
            throw new IllegalArgumentException("Invalid file size unit: " + maxImageSize);
        }
    }

    private String createMessage() {
        String validTypesList = String.join(", ", allowedTypes);
        return String.format(messageTemplate, validTypesList, maxImageSize);
    }

    private void addViolation(ConstraintValidatorContext context) {
        String message = createMessage();
        context.disableDefaultConstraintViolation();
        context.buildConstraintViolationWithTemplate(message)
            .addConstraintViolation();
    }

    private boolean isValidImage(MultipartFile image) {
        if (Objects.isNull(image)) {
            return true;
        }

        if (image.getSize() > getMaxSizeInBytes()) {
            return false;
        }

        String mimeType = image.getContentType();
        return allowedTypes.contains(mimeType);
    }
}
