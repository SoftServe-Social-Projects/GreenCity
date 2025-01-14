package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception we get when the user is not the current user.
 *
 * @version 1.0
 */
@StandardException
public class NotCurrentUserException extends RuntimeException {
}
