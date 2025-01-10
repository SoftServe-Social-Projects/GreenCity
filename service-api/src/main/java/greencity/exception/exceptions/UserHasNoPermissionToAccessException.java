package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when user has no permission for certain action.
 */
@StandardException
public class UserHasNoPermissionToAccessException extends RuntimeException {
}
