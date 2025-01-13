package greencity.exception.exceptions;

import lombok.experimental.StandardException;

@StandardException
/**
 * Exception thrown when a user already has a habit assigned.
 *
 * @version 1.0
 */

public class UserAlreadyHasHabitAssignedException extends BadRequestException {
}
