package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when we try saving some object but such object already
 * exist, then we get {@link NotSavedException}.
 *
 * @author Vitaliy Dzen
 * @version 1.0
 */
@StandardException
public class NotSavedException extends RuntimeException {
}