package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that is thrown when parsing of image's URL fails.
 */
@StandardException
public class ImageUrlParseException extends RuntimeException {
}
