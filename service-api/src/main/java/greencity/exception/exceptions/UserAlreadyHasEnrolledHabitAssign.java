package greencity.exception.exceptions;

import lombok.experimental.StandardException;

@StandardException
/**
 * Exception we get when a user already has an enrolled habit assign.
 *
 * @version 1.0
 */
public class UserAlreadyHasEnrolledHabitAssign extends BadRequestException {
}
