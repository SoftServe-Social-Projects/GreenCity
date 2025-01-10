package greencity.validator;

import greencity.annotations.ValidLanguage;
import greencity.service.LanguageService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LanguageValidator implements ConstraintValidator<ValidLanguage, Locale> {
    private final LanguageService languageService;
    private List<String> codes;

    @Override
    public void initialize(ValidLanguage constraintAnnotation) {
        codes = languageService.findAllLanguageCodes();
    }

    @Override
    public boolean isValid(Locale value, ConstraintValidatorContext context) {
        return codes.contains(value.getLanguage());
    }
}
