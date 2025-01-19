package greencity.exception.helper;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ExceptionResponseBuilder {
    public static ResponseEntity<Object> buildResponse(HttpStatus httpStatus, String error, String errorMessage,
        String url) {
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("timestamp", System.currentTimeMillis());
        response.put("status", httpStatus.value());
        response.put("error", error);
        response.put("message", errorMessage);
        response.put("path", url);
        return ResponseEntity.status(httpStatus).body(response);
    }
}