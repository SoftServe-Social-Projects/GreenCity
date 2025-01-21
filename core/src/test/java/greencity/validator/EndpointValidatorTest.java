package greencity.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class EndpointValidatorTest {

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(EndpointValidator.class, "validEndpoints",
            List.of("/api/test", "/api/valid", "friends/{friendId}/request/{id}"));
    }

    @Test
    void hasExtraCharactersWithValidEndpointTest() {
        boolean result = EndpointValidator.checkUrl("/api/testse");
        assertFalse(result, "Method should return false for valid URL");
    }

    @Test
    void hasExtraCharactersWithInvalidEndpointTest() {
        boolean result = EndpointValidator.checkUrl("/api/invalid");
        assertFalse(result, "Method should return false for invalid URL");
    }

    @Test
    void isValidEndpointWithValidIdTest() {
        boolean result = EndpointValidator.checkUrl("friends/2/request/3");
        assertTrue(result, "Method should return true for valid URL with numeric values");
    }

    @Test
    void isValidEndpointWithInvalidIdTest() {
        boolean result = EndpointValidator.checkUrl("friends/2/request/abc");
        assertFalse(result, "Method should return false for invalid URL with non-numeric value");
    }

    @Test
    void checkUrlWithValidEndpointTest() {
        boolean result = EndpointValidator.checkUrl("/api/test");
        assertTrue(result, "Method should return true for valid URL");
    }

    @Test
    void checkUrlWithValidEndpointWithVariablesTest() {
        boolean result = EndpointValidator.checkUrl("friends/2/request/1");
        assertTrue(result, "Method should return true for URL matching valid endpoint with variables");
    }

    @Test
    void checkUrlWithNonNumericIdTest() {
        boolean result = EndpointValidator.checkUrl("friends/2/request/abc");
        assertFalse(result, "Method should return false for invalid URL with non-numeric id");
    }
}
