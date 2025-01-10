package greencity.validator;

import greencity.annotations.ValidEcoNewsDtoRequest;
import greencity.constant.ValidationConstants;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;

public class EcoNewsDtoRequestValidator implements ConstraintValidator<ValidEcoNewsDtoRequest, AddEcoNewsDtoRequest> {
    @Override
    public boolean isValid(AddEcoNewsDtoRequest value, ConstraintValidatorContext context) {
        if (value.getSource() != null && !value.getSource().isEmpty()) {
            UrlValidator.isUrlValid(value.getSource());
        }
        List<String> tags = value.getTags();
        return !tags.isEmpty() && tags.size() <= ValidationConstants.MAX_AMOUNT_OF_TAGS;
    }
}