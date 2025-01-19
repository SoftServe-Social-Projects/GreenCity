package greencity.exception.handler.helper;

import greencity.exception.helper.ExceptionResponseBuilder;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(MockitoExtension.class)
class ExceptionResponseBuilderTest {
    private HttpStatus status;
    private String error;
    private String errorMessage;
    private String url;

    @BeforeEach
    void setUp() {
        status = HttpStatus.NOT_FOUND;
        error = "Not Found";
        errorMessage = "Resource not found";
        url = "/api/test";
    }

    @Test
    void BuildResponseWithValidParametersTest() {
        ResponseEntity<Object> response = ExceptionResponseBuilder.buildResponse(status, error, errorMessage, url);
        assertEquals(status, response.getStatusCode(), "The status code should match.");
        assertTrue(response.getBody() instanceof Map, "The response body should be a map.");
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();

        assertEquals(status.value(), responseBody.get("status"), "The status value should match.");
        assertEquals(error, responseBody.get("error"), "The error message should match.");
        assertEquals(errorMessage, responseBody.get("message"), "The error message should match.");
        assertEquals(url, responseBody.get("path"), "The path should match.");
        assertTrue(responseBody.get("timestamp") instanceof Long, "The timestamp should be a long value.");
    }

    @Test
    void BuildResponseWithDifferentStatusTest() {
        ResponseEntity<Object> response = ExceptionResponseBuilder.buildResponse(status, error, errorMessage, url);
        assertEquals(status, response.getStatusCode(), "The status code should match.");
    }

    @Test
    void BuildResponseWithEmptyErrorMessageTest() {
        ResponseEntity<Object> response = ExceptionResponseBuilder.buildResponse(status, error, errorMessage, url);
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(status.value(), responseBody.get("status"));
        assertEquals(error, responseBody.get("error"));
        assertEquals(errorMessage, responseBody.get("message"));
        assertEquals(url, responseBody.get("path"));
    }
}
