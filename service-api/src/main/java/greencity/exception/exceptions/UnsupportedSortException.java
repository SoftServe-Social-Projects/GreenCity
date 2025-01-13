package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that get if user write not supported sorting operation.
 *
 * @version 1.0
 */
@StandardException
public class UnsupportedSortException extends UnsupportedOperationException {
}
