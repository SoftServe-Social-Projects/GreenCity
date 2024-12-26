package greencity.exception.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Custom exception for the 404-status.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    /**
     * Constructor for the exception.
     * 
     * @param message — message of the exception
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
