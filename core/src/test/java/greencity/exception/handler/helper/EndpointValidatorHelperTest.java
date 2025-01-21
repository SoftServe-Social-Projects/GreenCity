package greencity.exception.handler.helper;

import greencity.constant.ErrorMessage;
import greencity.exception.helper.EndpointValidationHelper;
import greencity.validator.EndpointValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import java.util.List;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.powermock.api.mockito.PowerMockito.mock;

@ExtendWith(MockitoExtension.class)
class EndpointValidatorHelperTest {

    @Mock
    private EndpointValidator endpointValidator;

    @Mock
    private HttpRequestMethodNotSupportedException exception;

    @Mock
    private WebRequest webRequest;

    @Mock
    private HttpServletRequest servletRequest;

    @Mock
    private HttpHeaders httpHeaders;

    private List<String> allowedMethods;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        List<String> validEndpointsList = List.of("/api/test", "/api/invalid/extra");
        ReflectionTestUtils.setField(endpointValidator, "validEndpoints", validEndpointsList);
        allowedMethods = List.of("GET", "POST");
    }

    @Test
    void ResponseWithExtraCharactersTest() {
        String url = "/api/invalid/extraa";
        when(httpHeaders.getOrEmpty(HttpHeaders.ALLOW)).thenReturn(List.of("GET", "POST"));
        ServletWebRequest servletWebRequest = mock(ServletWebRequest.class);
        when(servletWebRequest.getRequest()).thenReturn(servletRequest);
        when(servletWebRequest.getDescription(false)).thenReturn("uri=" + url);
        try (MockedStatic<EndpointValidator> mocked = mockStatic(EndpointValidator.class)) {
            mocked.when(() -> EndpointValidator.checkUrl(url)).thenReturn(false);
            ResponseEntity<Object> response = EndpointValidationHelper.response(
                null, httpHeaders, servletWebRequest);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertTrue(response.getBody() instanceof Map, "The response body should be a map.");
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            assertEquals("Not Found", responseBody.get("error"));
            assertEquals(String.format("No endpoint found for %s", url), responseBody.get("message"));
        }
    }

    @Test
    void ResponseWithMethodNotAllowedTest() {
        String url = "/api/test";
        String method = "PUT";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(url);
        request.setMethod(method);
        ServletWebRequest servletWebRequest = new ServletWebRequest(request);
        when(httpHeaders.getOrEmpty("Allow")).thenReturn(allowedMethods);
        when(exception.getMethod()).thenReturn(method);
        ResponseEntity<Object> response = EndpointValidationHelper.response(
            exception, httpHeaders, servletWebRequest);
        assertEquals(HttpStatus.METHOD_NOT_ALLOWED, response.getStatusCode());
        assertTrue(response.getBody() instanceof Map, "The response body should be a map.");
        Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
        assertEquals(ErrorMessage.METHOD_NOT_ALLOWED, responseBody.get("error"));
        assertEquals(
            String.format("Method %s is not allowed for %s. Supported Methods: %s", method, url, allowedMethods),
            responseBody.get("message"));
    }

    @Test
    void ResponseWithDefaultConditionTest() {
        String url = "/api/test";
        String method = "POST";
        when(servletRequest.getMethod()).thenReturn(method);
        when(servletRequest.getRequestURI()).thenReturn(url);
        ServletWebRequest servletWebRequest = new ServletWebRequest(servletRequest);
        when(httpHeaders.getOrEmpty("Allow")).thenReturn(allowedMethods);
        ResponseEntity<Object> response = EndpointValidationHelper.response(
            exception, httpHeaders, servletWebRequest);
        assertNull(response, "Response should be null when no conditions are met.");
    }

    @Test
    void EvaluateConditionWithExtraCharactersTest() {
        String url = "/api/invalid/extraa";
        String method = "GET";
        String condition = EndpointValidationHelper.evaluateCondition(url, method, allowedMethods);
        assertEquals("extraCharacters", condition, "Condition should be extraCharacters.");
    }

    @Test
    void EvaluateConditionWithMethodNotAllowedTest() {
        String url = "/api/test";
        String method = "PUT";
        String condition = EndpointValidationHelper.evaluateCondition(url, method, allowedMethods);
        assertEquals("methodNotAllowed", condition, "Condition should be methodNotAllowed.");
    }

    @Test
    void GetUrlFromRequestTest() {
        String expectedUrl = "/api/test";
        when(webRequest.getDescription(false)).thenReturn("uri=" + expectedUrl);
        String actualUrl = EndpointValidationHelper.getUrlFromRequest(webRequest);
        assertEquals(expectedUrl, actualUrl, "The URL should match the expected.");
    }

    @Test
    void GetErrorMessageTest() {
        String url = "/api/test";
        String supportedMethods = "GET, POST";
        String method = "PUT";
        when(exception.getMethod()).thenReturn(method);
        String errorMessage = EndpointValidationHelper.getErrorMessage(exception, url, supportedMethods);
        assertEquals(
            String.format("Method %s is not allowed for %s. Supported Methods: %s", method, url, supportedMethods),
            errorMessage);
    }

    @Test
    void ResponseWithPlaceholderUrlTest() {
        String actualUrl = "/management/users/5/test";
        String method = "GET";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(actualUrl);
        request.setMethod(method);
        ServletWebRequest servletWebRequest = new ServletWebRequest(request);
        when(httpHeaders.getOrEmpty(HttpHeaders.ALLOW)).thenReturn(allowedMethods);
        try (MockedStatic<EndpointValidator> mockedValidator = mockStatic(EndpointValidator.class)) {
            mockedValidator.when(() -> EndpointValidator.checkUrl(actualUrl)).thenReturn(true);
            ResponseEntity<Object> response = EndpointValidationHelper.response(
                null, httpHeaders, servletWebRequest);
            assertNull(response, "Response should be null for valid placeholder URL.");
            mockedValidator.verify(() -> EndpointValidator.checkUrl(actualUrl), times(1));
        }
    }

    @Test
    void ResponseWithInvalidPlaceholderUrlTest() {
        String url = "/management/users/{id}/delete-filter";
        String actualUrl = "/management/users/{invalid}/delete-filtr";
        String method = "GET";
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setRequestURI(actualUrl);
        request.setMethod(method);
        ServletWebRequest servletWebRequest = new ServletWebRequest(request);
        when(httpHeaders.getOrEmpty(HttpHeaders.ALLOW)).thenReturn(allowedMethods);
        try (MockedStatic<EndpointValidator> mocked = mockStatic(EndpointValidator.class)) {
            mocked.when(() -> EndpointValidator.checkUrl(url)).thenReturn(false);
            ResponseEntity<Object> response = EndpointValidationHelper.response(
                null, httpHeaders, servletWebRequest);
            assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
            assertInstanceOf(Map.class, response.getBody(), "The response body should be a map.");
            Map<String, Object> responseBody = (Map<String, Object>) response.getBody();
            assertEquals("Not Found", responseBody.get("error"));
            assertEquals(String.format("No endpoint found for %s", actualUrl), responseBody.get("message"));
        }
    }

    @Test
    void EvaluateConditionWithPlaceholderUrlTest() {
        String actualUrl = "/management/users/456/friends";
        String method = "GET";
        try (MockedStatic<EndpointValidator> mocked = mockStatic(EndpointValidator.class)) {
            mocked.when(() -> EndpointValidator.checkUrl(actualUrl)).thenReturn(true);
            String condition = EndpointValidationHelper.evaluateCondition(actualUrl, method, allowedMethods);
            assertEquals("default", condition, "Condition should be default for valid placeholder URL.");
        }
    }

    @Test
    void EvaluateConditionWithInvalidPlaceholderUrlTest() {
        String actualUrl = "/place/{invalid-status}";
        String method = "POST";
        try (MockedStatic<EndpointValidator> mocked = mockStatic(EndpointValidator.class)) {
            mocked.when(() -> EndpointValidator.checkUrl(actualUrl)).thenReturn(false);
            String condition = EndpointValidationHelper.evaluateCondition(actualUrl, method, allowedMethods);
            assertEquals("extraCharacters", condition,
                "Condition should be extraCharacters for invalid placeholder URL.");
        }
    }

}