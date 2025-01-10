package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when user has no friend with certain id.
 *
 * @author Volodymyr Kharchenko.
 * @version 1.0.
 */
@StandardException
public class UserHasNoFriendWithIdException extends RuntimeException {
}
