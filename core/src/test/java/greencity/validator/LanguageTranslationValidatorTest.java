package greencity.validator;

import static greencity.ModelUtils.getLanguageDTO;
import static greencity.ModelUtils.getLanguageTranslationDTO;
import greencity.dto.language.LanguageDTO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.service.LanguageService;
import java.util.Arrays;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.powermock.api.mockito.PowerMockito.when;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class LanguageTranslationValidatorTest {
    @InjectMocks
    private LanguageTranslationValidator languageTranslationValidator;
    @Mock
    private LanguageService languageService;

    @Test
    void isValidTrueTest() {
        LanguageTranslationDTO ltDTO = getLanguageTranslationDTO();
        LanguageDTO lDTO = getLanguageDTO();
        List<LanguageTranslationDTO> value = Arrays.asList(ltDTO, ltDTO);
        List<LanguageDTO> languageDTOS = Arrays.asList(lDTO, lDTO);
        when(languageService.getAllLanguages()).thenReturn(languageDTOS);
        languageTranslationValidator.initialize(null);
        assertTrue(languageTranslationValidator.isValid(value, null));
    }

    @Test
    void isValidFalseTest() {
        LanguageTranslationDTO ltDTO = getLanguageTranslationDTO();
        LanguageDTO lDTO = getLanguageDTO();
        List<LanguageTranslationDTO> value = Arrays.asList(ltDTO, ltDTO, ltDTO, ltDTO);
        List<LanguageDTO> languageDTOS = Arrays.asList(lDTO, lDTO, lDTO, lDTO);
        when(languageService.getAllLanguages()).thenReturn(languageDTOS);
        languageTranslationValidator.initialize(null);
        assertFalse(languageTranslationValidator.isValid(value, null));
    }
}
