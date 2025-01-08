package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when moderator trying to update user status of admin or
 * another moderator.
 *
 * @author Rostyslav Khasanov
 */
@StandardException
public class LowRoleLevelException extends RuntimeException {
}
