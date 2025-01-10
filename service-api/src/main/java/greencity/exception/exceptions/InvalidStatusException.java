package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception is thrown when entity have invalid status for current operation.
 */
@StandardException
public class InvalidStatusException extends RuntimeException {
}
