package greencity.service;

import greencity.dto.language.LanguageDTO;
import java.util.List;

/**
 * LanguageService interface.
 *
 * @author Vitaliy Dzen
 */
public interface LanguageService {
    /**
     * Method finds all {@link LanguageDTO}.
     *
     * @return List of all {@link LanguageDTO}
     * @author Vitaliy Dzen
     */
    List<LanguageDTO> getAllLanguages();

    /**
     * Method for getting {@link LanguageDTO} by code.
     *
     * @param code code of language.
     * @return {@link LanguageDTO} by language code.
     */
    LanguageDTO findByCode(String code);

    /**
     * Method, that returns codes of all languages.
     *
     * @return {@link List} of language code strings.
     */
    List<String> findAllLanguageCodes();
}
