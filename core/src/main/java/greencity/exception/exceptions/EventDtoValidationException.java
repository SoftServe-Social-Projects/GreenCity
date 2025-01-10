package greencity.exception.exceptions;

import jakarta.validation.ValidationException;
import lombok.experimental.StandardException;

@StandardException
public class EventDtoValidationException extends ValidationException {
}
