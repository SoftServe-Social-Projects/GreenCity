package greencity.validator;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class EndpointValidatorTest {
    @Mock
    private EndpointValidator endpointValidator;

    @BeforeEach
    void setUp() {
        endpointValidator =
            new EndpointValidator(List.of("/api/test", "/api/valid", "friends/{friendId}/request/{id}"));
        ReflectionTestUtils.setField(EndpointValidator.class, "validEndpoints",
            List.of("/api/test", "/api/valid", "friends/{friendId}/request/{id}"));
    }

    @Test
    void hasExtraCharactersWithValidEndpointTest() {
        boolean result = EndpointValidator.hasExtraCharacters("/api/testse");
        assertTrue(result, "Method should return false for valid URL");
    }

    @Test
    void hasExtraCharactersWithInvalidEndpointTest() {
        boolean result = EndpointValidator.hasExtraCharacters("/api/invalid");
        assertTrue(result, "Method should return true for invalid URL");
    }

    @Test
    void isValidEndpoint_withValidIdTest() {
        boolean result = EndpointValidator.isValidEndpoint("friends/{friendId}/request/{id}", "friends/2/request/3");
        assertTrue(result, "Method should return true for valid URL with numeric values");
    }

    @Test
    void isValidEndpoint_withInvalidIdTest() {
        boolean result = EndpointValidator.isValidEndpoint("friends/{friendId}/request/{id}", "friends/2/request/abc");
        assertFalse(result, "Method should return false for invalid URL with non-numeric value");
    }

    @Test
    void checkUrl_withValidEndpointTest() {
        boolean result = EndpointValidator.checkUrl("/api/test");
        assertTrue(result, "Method should return true for valid URL");
    }

    @Test
    void checkUrl_withValidEndpointWithVariablesTest() {
        boolean result = EndpointValidator.checkUrl("friends/2/request/1");
        assertTrue(result, "Method should return true for URL matching valid endpoint with variables");
    }

    @Test
    void checkUrl_withInvalidEndpointTest() {
        boolean result = EndpointValidator.checkUrl("/api/invalid");
        assertFalse(result, "Method should return false for invalid URL");
    }

    @Test
    void checkUrl_withNonNumericIdTest() {
        boolean result = EndpointValidator.checkUrl("friends/2/request/abc");
        assertFalse(result, "Method should return false for invalid URL with non-numeric id");
    }
}
