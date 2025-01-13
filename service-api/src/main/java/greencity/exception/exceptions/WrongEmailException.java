package greencity.exception.exceptions;

import lombok.experimental.StandardException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Exception that we get when user by this email not found.
 *
 * @version 1.0
 */
@StandardException
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class WrongEmailException extends BadRequestException {
}
