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

    private EndpointValidator endpointValidator;

    @BeforeEach
    void setUp() {
        endpointValidator =
            new EndpointValidator(List.of("/api/test", "/api/valid", "friends/{friendId}/request/{id}"));
        ReflectionTestUtils.setField(EndpointValidator.class, "instance", endpointValidator);
    }

    // Тест для перевірки на зайві символи (метод hasExtraCharacters)
    @Test
    void hasExtraCharacters_withValidEndpointTest() {
        boolean result = EndpointValidator.hasExtraCharacters("/api/test");
        assertFalse(result, "Method should return false for valid URL"); // Очікується false для валідного URL
    }

    @Test
    void hasExtraCharacters_withInvalidEndpointTest() {
        boolean result = EndpointValidator.hasExtraCharacters("/api/invalid");
        assertTrue(result, "Method should return true for invalid URL"); // Очікується true для невалідного URL
    }

    // Тест для перевірки методу isValidEndpoint (вірність ендпоінту з підставленими
    // змінними)
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

    // Тест для основного методу checkUrl
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
