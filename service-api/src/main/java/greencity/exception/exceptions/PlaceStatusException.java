package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that is thrown when an entity has an invalid status for the current
 * operation, specifically related to place status.
 *
 * @version 1.0
 */
@StandardException
public class PlaceStatusException extends RuntimeException {
}
