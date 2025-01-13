package greencity.service;

import greencity.ModelUtils;
import greencity.dto.language.LanguageDTO;
import greencity.entity.Language;
import greencity.exception.exceptions.LanguageNotFoundException;
import greencity.repository.LanguageRepo;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

@ExtendWith(MockitoExtension.class)
class LanguageServiceImplTest {

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private LanguageRepo languageRepo;

    @InjectMocks
    private LanguageServiceImpl languageService;

    private final Language language = ModelUtils.getLanguage();

    @Test
    void getAllLanguages() {
        List<LanguageDTO> expected = Collections.emptyList();
        when(modelMapper.map(languageRepo.findAll(), new TypeToken<List<LanguageDTO>>() {
        }.getType())).thenReturn(expected);
        assertEquals(expected, languageService.getAllLanguages());
    }

    @Test
    void findByCode() {
        LanguageDTO dto = new LanguageDTO(1L, "en");
        when(languageRepo.findByCode(language.getCode())).thenReturn(Optional.of(language));
        when(modelMapper.map(language, LanguageDTO.class)).thenReturn(dto);
        assertEquals(dto, languageService.findByCode(language.getCode()));
    }

    @Test
    void findCodeByIdFailed() {
        Assertions.assertThrows(LanguageNotFoundException.class, () -> languageService.findByCode("ua"));
    }

    @Test
    void findAllLanguageCodes() {
        List<String> code = Collections.singletonList(language.getCode());
        when(languageRepo.findAllLanguageCodes()).thenReturn(code);
        assertEquals(code, languageService.findAllLanguageCodes());
    }
}
