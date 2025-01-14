package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception we get when an unsubscribe token is invalid.
 *
 * @version 1.0
 */
@StandardException
public class InvalidUnsubscribeToken extends BadRequestException {
}
