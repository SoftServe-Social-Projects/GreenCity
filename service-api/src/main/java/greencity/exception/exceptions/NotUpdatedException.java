package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when we try updating some object but such object not
 * exist, then we get {@link NotUpdatedException}.
 *
 * @author Vitaliy Dzen
 * @version 1.0
 */
@StandardException
public class NotUpdatedException extends RuntimeException {
}