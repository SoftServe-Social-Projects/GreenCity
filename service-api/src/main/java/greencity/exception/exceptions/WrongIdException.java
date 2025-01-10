package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when in some logic we have bad ID.
 *
 * @author Nazar Stasyuk
 */
@StandardException
public class WrongIdException extends RuntimeException {
}
