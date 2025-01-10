package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when admin/moderator trying to update himself.
 *
 * @author Rostyslav Khasanov
 */
@StandardException
public class BadUpdateRequestException extends RuntimeException {
}
