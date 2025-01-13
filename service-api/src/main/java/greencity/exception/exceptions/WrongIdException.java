package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when in some logic we have bad ID.
 *
 * @author Nazar Stasyuk
 * @version 1.0
 */
@StandardException
public class WrongIdException extends BadRequestException {
}
