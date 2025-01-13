package greencity.exception.exceptions;

import jakarta.validation.ConstraintDeclarationException;
import lombok.experimental.StandardException;

/**
 * Exception we get when we receive wrong social network links.
 *
 * @version 1.0
 */
@StandardException
public class BadSocialNetworkLinksException extends ConstraintDeclarationException {
    public BadSocialNetworkLinksException(String message) {
        super(message);
    }
}
