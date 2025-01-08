package greencity.validator;

import greencity.annotations.LanguageTranslationConstraint;
import greencity.dto.language.LanguageDTO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.service.LanguageService;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class LanguageTranslationValidator implements
    ConstraintValidator<LanguageTranslationConstraint, List<? extends LanguageTranslationDTO>> {
    private final LanguageService languageService;
    private List<LanguageDTO> languageDTOS;

    @Override
    public void initialize(LanguageTranslationConstraint constraintAnnotation) {
        this.languageDTOS = languageService.getAllLanguages();
        this.languageDTOS.sort(Comparator.comparing(LanguageDTO::getCode));
    }

    @Override
    public boolean isValid(List<? extends LanguageTranslationDTO> value, ConstraintValidatorContext context) {
        List<LanguageDTO> valueLanguageDTOS = value
            .stream()
            .map(LanguageTranslationDTO::getLanguage)
            .sorted(Comparator.comparing(LanguageDTO::getCode))
            .toList();

        return (value.size() == 2) && (languageDTOS.equals(valueLanguageDTOS));
    }
}
