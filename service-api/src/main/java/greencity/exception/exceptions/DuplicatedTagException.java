package greencity.exception.exceptions;

import jakarta.validation.ConstraintDeclarationException;
import lombok.experimental.StandardException;

/**
 * Exception we get when we try to use not unique (duplicated) tags.
 *
 * @version 1.0
 */
@StandardException
public class DuplicatedTagException extends ConstraintDeclarationException {
}
