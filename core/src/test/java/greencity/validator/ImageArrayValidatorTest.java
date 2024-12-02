package greencity.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import java.util.List;
import static org.junit.Assert.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ImageArrayValidatorTest {
    @InjectMocks
    ImageArrayValidator imageArrayValidator;

    @Mock
    ConstraintValidatorContext constraintValidatorContext;

    @Mock
    ConstraintValidatorContext.ConstraintViolationBuilder violationBuilder;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(imageArrayValidator, "maxImageSize", "10MB");
        ReflectionTestUtils.setField(imageArrayValidator, "messageTemplate", "Upload %s only. Max size of %s each.");
        ReflectionTestUtils.setField(imageArrayValidator, "allowedTypes",
            List.of("image/jpeg", "image/png", "image/jpg", "image/gif"));
    }

    @Test
    void validWithValidImageJPEGReturnsTrue() {
        MockMultipartFile[] image = new MockMultipartFile[] {
            new MockMultipartFile("image", "image.jpeg", "image/jpeg", new byte[1024])};
        assertTrue(imageArrayValidator.isValid(image, constraintValidatorContext));
    }

    @Test
    void validWithValidImageJPGReturnsTrue() {
        MockMultipartFile[] image = new MockMultipartFile[] {
            new MockMultipartFile("image", "image.jpg", "image/jpg", new byte[10 * 1024 * 1024])}; // max size
        assertTrue(imageArrayValidator.isValid(image, constraintValidatorContext));
    }

    @Test
    void validWithValidImagePNGReturnsTrue() {
        MockMultipartFile[] image = new MockMultipartFile[] {
            new MockMultipartFile("image", "image.png", "image/png", new byte[1])};
        assertTrue(imageArrayValidator.isValid(image, constraintValidatorContext));
    }

    @Test
    void validWithValidImageGIFReturnsTrue() {
        MockMultipartFile[] image = new MockMultipartFile[] {
            new MockMultipartFile("image", "image.gif", "image/gif", new byte[1024])};
        assertTrue(imageArrayValidator.isValid(image, constraintValidatorContext));
    }

    @Test
    void isValid_WithEmptyImageReturnsTrue() {
        MockMultipartFile[] image = new MockMultipartFile[] {
            new MockMultipartFile("image", "image.jpeg", "image/jpeg", new byte[0])};
        assertTrue(imageArrayValidator.isValid(image, constraintValidatorContext));
    }

    @Test
    void isValid_WithNullImageReturnsTrue() {
        assertTrue(imageArrayValidator.isValid(null, constraintValidatorContext));
    }

    @Test
    void isValid_WithNullElementsReturnsTrue() {
        MockMultipartFile[] image = new MockMultipartFile[1];
        image[0] = null;

        boolean result = imageArrayValidator.isValid(image, constraintValidatorContext);
        assertTrue(result);
    }

    @Test
    void validWithInvalidContentTypeReturnsFalse() {
        MockMultipartFile[] image = new MockMultipartFile[] {
            new MockMultipartFile("image", "image.txt", "text/plain", new byte[0])};
        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
            .thenReturn(violationBuilder);

        assertFalse(imageArrayValidator.isValid(image, constraintValidatorContext));
    }

    @Test
    void validWithInvalidSizeReturnsFalse() {
        int invalidSize = (10 * 1024 * 1024) + 1;
        MockMultipartFile[] image = new MockMultipartFile[] {
            new MockMultipartFile("image", "image.jpeg", "image/jpeg", new byte[invalidSize])};

        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
            .thenReturn(violationBuilder);

        assertFalse(imageArrayValidator.isValid(image, constraintValidatorContext));

        verify(constraintValidatorContext).disableDefaultConstraintViolation();
        verify(constraintValidatorContext)
            .buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void validWithGifExceedingSizeReturnsFalse() {
        int invalidSize = 15 * 1024 * 1024;
        MockMultipartFile[] image = new MockMultipartFile[] {
            new MockMultipartFile("image", "image.gif", "image/gif", new byte[invalidSize])};

        when(constraintValidatorContext.buildConstraintViolationWithTemplate(anyString()))
            .thenReturn(violationBuilder);

        assertFalse(imageArrayValidator.isValid(image, constraintValidatorContext));

        verify(constraintValidatorContext).disableDefaultConstraintViolation();
        verify(constraintValidatorContext)
            .buildConstraintViolationWithTemplate(anyString());
    }

    @Test
    void getMaxSizeInBytes_WithInvalidUnit_ThrowsException() {
        ReflectionTestUtils.setField(imageArrayValidator, "maxImageSize", "500TB"); // Invalid unit
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            imageArrayValidator.getMaxSizeInBytes();
        });
        Assertions.assertEquals("Invalid file size unit: 500TB", exception.getMessage());
    }

    @Test
    void getMaxSizeInBytes_WithDifferentUnits_CorrectConversion() {
        ReflectionTestUtils.setField(imageArrayValidator, "maxImageSize", "1024KB");
        Assertions.assertEquals(1024 * 1024, imageArrayValidator.getMaxSizeInBytes());

        ReflectionTestUtils.setField(imageArrayValidator, "maxImageSize", "5MB");
        Assertions.assertEquals(5 * 1024 * 1024, imageArrayValidator.getMaxSizeInBytes());

        ReflectionTestUtils.setField(imageArrayValidator, "maxImageSize", "1GB");
        Assertions.assertEquals(1024 * 1024 * 1024, imageArrayValidator.getMaxSizeInBytes());

        ReflectionTestUtils.setField(imageArrayValidator, "maxImageSize", "500TB");

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            imageArrayValidator.getMaxSizeInBytes();
        });
        Assertions.assertEquals("Invalid file size unit: 500TB", exception.getMessage());

    }
}
