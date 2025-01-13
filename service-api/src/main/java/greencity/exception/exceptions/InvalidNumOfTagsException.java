package greencity.exception.exceptions;

import jakarta.validation.ConstraintDeclarationException;
import lombok.experimental.StandardException;

/**
 * Exception we get when we try to use more than 3 tags.
 *
 * @version 1.0
 */
@StandardException
public class InvalidNumOfTagsException extends ConstraintDeclarationException {
}
