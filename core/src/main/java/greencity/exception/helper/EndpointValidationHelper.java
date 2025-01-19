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

    private EndpointValidationHelper() {
    }

    public static ResponseEntity<Object> response(HttpRequestMethodNotSupportedException ex, HttpHeaders headers,
        WebRequest request) {
        String url = getUrlFromRequest(request);
        ServletWebRequest servletWebRequest = (ServletWebRequest) request;
        HttpServletRequest servletRequest = servletWebRequest.getRequest();

        String method = servletRequest.getMethod();
        List<String> list = headers.getOrEmpty("Allow");

        switch (getCondition(url, method, list)) {
            case EXTRA_CHARACTERS:
                String notFoundErrorMessage = String.format("No endpoint found for %s", url);
                return ExceptionResponseBuilder.buildResponse(HttpStatus.NOT_FOUND, "Not Found",
                    notFoundErrorMessage, url);

            case METHOD_NOT_ALLOWED:
                String methodNotAllowedErrorMessage = getErrorMessage(ex, url, list.toString());
                return ExceptionResponseBuilder.buildResponse(HttpStatus.METHOD_NOT_ALLOWED,
                    ErrorMessage.METHOD_NOT_ALLOWED,
                    methodNotAllowedErrorMessage, url);

            default:
                return null;
        }
    }

    public static String getUrlFromRequest(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }

    public static String getCondition(String url, String method, List<String> list) {
        if (!EndpointValidator.checkUrl(url)) {
            return EXTRA_CHARACTERS;
        }
        if (!list.contains(method)) {
            return METHOD_NOT_ALLOWED;
        }
        return "default";
    }

    public static String getErrorMessage(HttpRequestMethodNotSupportedException ex, String url,
        String supportedMethods) {
        return String.format(
            "Method %s is not allowed for %s. Supported Methods: %s",
            ex.getMethod(), url, supportedMethods);
    }
}
