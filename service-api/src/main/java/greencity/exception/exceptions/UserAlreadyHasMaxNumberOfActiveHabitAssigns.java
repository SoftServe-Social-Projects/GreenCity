package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception thrown when a user already has the maximum number of active habit
 * assignments.
 *
 * @version 1.0
 */
@StandardException
public class UserAlreadyHasMaxNumberOfActiveHabitAssigns extends BadRequestException {
}
