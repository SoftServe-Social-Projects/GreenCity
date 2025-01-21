package greencity.exception.helper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ExceptionResponseBuilder {
    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String ERROR = "error";
    private static final String MESSAGE = "message";
    private static final String PATH = "path";

    private ExceptionResponseBuilder() {
    }

    public static ResponseEntity<Object> buildResponse(HttpStatus httpStatus, String error, String errorMessage,
        String url) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put(TIMESTAMP, System.currentTimeMillis());
        response.put(STATUS, httpStatus.value());
        response.put(ERROR, error);
        response.put(MESSAGE, errorMessage);
        response.put(PATH, url);
        return ResponseEntity.status(httpStatus).body(response);
    }
}