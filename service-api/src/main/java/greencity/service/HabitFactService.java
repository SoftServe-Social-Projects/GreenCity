package greencity.service;

import greencity.dto.habitfact.HabitFactDto;
import greencity.dto.habitfact.HabitFactPostDto;
import greencity.dto.habitfact.HabitFactVO;
import greencity.dto.language.LanguageTranslationDTO;
import java.util.List;

/**
 * HabitFactService interface.
 *
 * @author Vitaliy Dzen
 */
public interface HabitFactService {
    /**
     * Method finds all {HabitFact}.
     *
     * @return List of all {@link LanguageTranslationDTO}
     * @author Vitaliy Dzen
     */
    List<LanguageTranslationDTO> getAllHabitFacts();

    /**
     * Method finds random {HabitFact}.
     *
     * @return random {@link LanguageTranslationDTO}
     * @author Vitaliy Dzen
     */
    LanguageTranslationDTO getRandomHabitFactByHabitIdAndLanguage(Long id, String language);

    /**
     * Method find {HabitFact} by id.
     *
     * @param id of {HabitFact}
     * @return {@link HabitFactDto}
     * @author Vitaliy Dzen
     */
    HabitFactDto getHabitFactById(Long id);

    /**
     * Method find {HabitFact} by habitfact.
     *
     * @param name of {HabitFact}
     * @return {@link HabitFactDto}
     * @author Vitaliy Dzen
     */
    HabitFactDto getHabitFactByName(String language, String name);

    /**
     * Method saves new {HabitFact}.
     *
     * @param fact {@link HabitFactPostDto}
     * @return instance of {HabitFact}
     * @author Vitaliy Dzen
     */
    HabitFactVO save(HabitFactPostDto fact);

    /**
     * Method updates {HabitFact}.
     *
     * @param fact {@link HabitFactPostDto}
     * @return instance of {HabitFactVO}
     * @author Vitaliy Dzen
     */
    HabitFactVO update(HabitFactPostDto fact, Long id);

    /**
     * Method delete {HabitFact} by id.
     *
     * @param id Long
     * @return id of deleted {HabitFact}
     * @author Vitaliy Dzen
     */
    Long delete(Long id);
}
