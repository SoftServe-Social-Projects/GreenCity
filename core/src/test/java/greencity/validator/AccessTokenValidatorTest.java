package greencity.validator;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class AccessTokenValidatorTest {
    @InjectMocks
    AccessTokenValidator validator;

    @Mock
    ConstraintValidatorContext context;

    @Test
    void testValidAccessToken() {
        String validToken = "abc-123._~+";
        assertTrue(validator.isValid(validToken, context));
    }

    @Test
    void testNullAccessToken() {
        String nullToken = null;
        assertFalse(validator.isValid(nullToken, context));
    }

    @Test
    void testInvalidAccessTokenWithSpecialCharacters() {
        String invalidToken = "abc@123";
        assertFalse(validator.isValid(invalidToken, context));
    }

    @Test
    void testEmptyAccessToken() {
        String emptyToken = "";
        assertFalse(validator.isValid(emptyToken, context));
    }

    @Test
    void testAccessTokenWithOnlyValidCharacters() {
        String onlyValidCharacters = "ABCDEF1234567890-._~+";
        assertTrue(validator.isValid(onlyValidCharacters, context));
    }
}
