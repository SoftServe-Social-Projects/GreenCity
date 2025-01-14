package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception thrown when a user has reached beyond the allowed enrollment range.
 *
 * @version 1.0
 */
@StandardException
public class UserHasReachedOutOfEnrollRange extends BadRequestException {
}
