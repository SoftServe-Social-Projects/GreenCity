package greencity.exception.helper;

import greencity.constant.ErrorMessage;
import greencity.validator.EndpointValidator;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import java.util.List;

@Component
public class EndpointValidationHelper {
    private static final String METHOD_NOT_ALLOWED = "methodNotAllowed";
    private static final String EXTRA_CHARACTERS = "extraCharacters";
    private static final String DEFAULT_CONDITION = "default";

    private EndpointValidationHelper() {
    }

    public static ResponseEntity<Object> response(HttpRequestMethodNotSupportedException ex, HttpHeaders headers,
        WebRequest request) {
        String url = getUrlFromRequest(request);
        String method;

        if (request instanceof ServletWebRequest servletWebRequest) {
            HttpServletRequest servletRequest = servletWebRequest.getRequest();
            method = servletRequest.getMethod();
        } else {
            throw new IllegalArgumentException(ErrorMessage.INVALID_REQUEST_TYPE);
        }
        List<String> allowedMethod = headers.getOrEmpty(HttpHeaders.ALLOW);

        return switch (evaluateCondition(url, method, allowedMethod)) {
            case EXTRA_CHARACTERS -> {
                String notFoundErrorMessage = String.format(ErrorMessage.NOT_FOUND_ENDPOINT_FOR_URL, url);
                yield ExceptionResponseBuilder.buildResponse(HttpStatus.NOT_FOUND, ErrorMessage.NOT_FOUND,
                    notFoundErrorMessage, url);
            }
            case METHOD_NOT_ALLOWED -> {
                String methodNotAllowedErrorMessage = getErrorMessage(ex, url, allowedMethod.toString());
                yield ExceptionResponseBuilder.buildResponse(HttpStatus.METHOD_NOT_ALLOWED,
                    ErrorMessage.METHOD_NOT_ALLOWED,
                    methodNotAllowedErrorMessage, url);
            }
            default -> null;
        };
    }

    public static String getUrlFromRequest(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    public static String evaluateCondition(String url, String method, List<String> allowedMethod) {
        if (!EndpointValidator.checkUrl(url)) {
            return EXTRA_CHARACTERS;
        }
        if (!allowedMethod.contains(method)) {
            return METHOD_NOT_ALLOWED;
        }
        return DEFAULT_CONDITION;
    }

    public static String getErrorMessage(HttpRequestMethodNotSupportedException ex, String url,
        String supportedMethods) {
        return String.format(
            ErrorMessage.METHOD_NOT_ALLOWED_FOR_URL,
            ex.getMethod(), url, supportedMethods);
    }
}
