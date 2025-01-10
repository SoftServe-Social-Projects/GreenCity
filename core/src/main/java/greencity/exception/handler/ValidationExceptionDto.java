package greencity.exception.handler;

import java.io.Serializable;
import lombok.Data;
import org.springframework.validation.FieldError;

/**
 * Dto for sending information about bad fields while validation.
 *
 * @author Nazar Stasyuk
 */
@Data
public class ValidationExceptionDto implements Serializable {
    private String name;
    private String message;

    /**
     * Constructs a new ValidationExceptionDto using the provided FieldError.
     *
     * @param error the FieldError instance containing information about the
     *              validation issue
     */
    public ValidationExceptionDto(FieldError error) {
        this.name = error.getField();
        this.message = error.getDefaultMessage();
    }
}
