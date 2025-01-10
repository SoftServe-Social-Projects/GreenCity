package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when we try deleting some object but such object not
 * exist, then we get {@link NotDeletedException}.
 *
 * @author Vitaliy Dzen
 * @version 1.0
 */
@StandardException
public class NotDeletedException extends RuntimeException {
}