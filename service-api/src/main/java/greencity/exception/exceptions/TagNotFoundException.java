package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when we try to get a tag that is not in the database.
 *
 * @version 1.0
 */
@StandardException
public class TagNotFoundException extends RuntimeException {
}
