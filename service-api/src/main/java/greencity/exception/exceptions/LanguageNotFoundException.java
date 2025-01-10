package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception, that is throw after saving object with language code, that doesn't
 * exist in database.
 */
@StandardException
public class LanguageNotFoundException extends RuntimeException {
}
