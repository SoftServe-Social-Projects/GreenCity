package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when user trying to add place or left comment that is
 * blocked.
 *
 * @author Kateryna Horokh
 * @version 1.0
 */
@StandardException
public class UserBlockedException extends RuntimeException {
}
