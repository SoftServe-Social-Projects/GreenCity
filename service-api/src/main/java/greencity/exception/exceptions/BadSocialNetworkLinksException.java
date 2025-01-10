package greencity.exception.exceptions;

import jakarta.validation.ConstraintDeclarationException;
import lombok.experimental.StandardException;

@StandardException
public class BadSocialNetworkLinksException extends ConstraintDeclarationException {
    public BadSocialNetworkLinksException(String message) {
        super(message);
    }
}
