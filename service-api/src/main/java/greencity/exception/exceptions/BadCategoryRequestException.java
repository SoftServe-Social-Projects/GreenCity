package greencity.exception.exceptions;

import lombok.experimental.StandardException;

/**
 * Exception that we get when user trying to save category with bad parameters.
 *
 * @author Kateryna Horokh
 */
@StandardException
public class BadCategoryRequestException extends RuntimeException {
}
