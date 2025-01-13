package greencity.exception.exceptions;

import jakarta.validation.ValidationException;
import lombok.experimental.StandardException;

/**
 * Exception thrown when an invalid EventDto is passed in a request. This
 * exception indicates that the provided EventDto does not meet the required
 * validation constraints.
 *
 */
@StandardException
public class EventDtoValidationException extends ValidationException {
}
