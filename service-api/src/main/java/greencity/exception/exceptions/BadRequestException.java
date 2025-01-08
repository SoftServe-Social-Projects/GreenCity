package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when user trying to pass bad request.
 */
@StandardException
public class BadRequestException extends RuntimeException {
}
