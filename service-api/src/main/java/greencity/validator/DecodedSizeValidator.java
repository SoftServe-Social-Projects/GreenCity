package greencity.validator;

import greencity.annotations.DecodedSize;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.jsoup.parser.Parser;

public class DecodedSizeValidator implements ConstraintValidator<DecodedSize, String> {
    private int max;
    private int min;

    @Override
    public void initialize(DecodedSize constraintAnnotation) {
        this.max = constraintAnnotation.max();
        this.min = constraintAnnotation.min();
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        String decodedString = Parser.unescapeEntities(value, true);
        int length = decodedString.length();

        return length >= min && length <= max;
    }
}
