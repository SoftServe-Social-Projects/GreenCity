package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.factoftheday.*;
import greencity.dto.tag.TagDto;
import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Set;

public interface FactOfTheDayService {
    /**
     * Method finds all {@link FactOfTheDayVO} with pageable configuration.
     *
     * @param pageable {@link Pageable}
     * @return {@link PageableDto} with list of all {@link FactOfTheDayDTO}
     * @author Mykola Lehkyi
     */
    PageableDto<FactOfTheDayDTO> getAllFactsOfTheDay(Pageable pageable);

    /**
     * Method saves new {@link FactOfTheDayVO} and
     * {@link FactOfTheDayTranslationVO}.
     *
     * @param fact {@link FactOfTheDayPostDTO}
     * @return instance of {@link FactOfTheDayPostDTO}
     * @author Mykola Lehkyi
     */
    FactOfTheDayPostDTO saveFactOfTheDayAndTranslations(FactOfTheDayPostDTO fact);

    /**
     * Method saves new {@link FactOfTheDayVO} and
     * {@link FactOfTheDayTranslationVO}.
     *
     * @param fact {@link FactOfTheDayPostDTO}
     * @return instance of {@link FactOfTheDayPostDTO}
     * @author Mykola Lehkyi
     */
    FactOfTheDayPostDTO updateFactOfTheDayAndTranslations(FactOfTheDayPostDTO fact);

    /**
     * Method deletes {@link FactOfTheDayVO} and {@link FactOfTheDayTranslationVO}
     * by id.
     *
     * @param id Long
     * @return id of deleted {@link FactOfTheDayVO}
     * @author Mykola Lehkyi
     */
    Long deleteFactOfTheDayAndTranslations(Long id);

    /**
     * Method deletes all {@link FactOfTheDayVO} {@link FactOfTheDayVO} and
     * {@link FactOfTheDayTranslationVO} by list of IDs.
     *
     * @param listId list of id {@link FactOfTheDayVO}
     * @return listId list of id {@link FactOfTheDayVO}
     * @author Mykola Lehkyi
     */
    List<Long> deleteAllFactOfTheDayAndTranslations(List<Long> listId);

    /**
     * Method returns pageable of {@link FactOfTheDayDTO} that satisfy search query.
     *
     * @param searchQuery query to search
     * @return pageable of {@link FactOfTheDayDTO}
     */
    PageableDto<FactOfTheDayDTO> searchBy(Pageable pageable, String searchQuery);

    /**
     * Returns a random {@link FactOfTheDayTranslationDTO} based on the provided
     * language code and habit tags.
     *
     * @param tagsId a set of tag IDs associated with habits
     * @return a random fact of the day
     */
    FactOfTheDayTranslationDTO getRandomFactOfTheDayByTags(Set<Long> tagsId);

    /**
     * Returns a random general {@link FactOfTheDayTranslationDTO} for the specified
     * language.
     *
     * @return a random general fact of the day
     */
    FactOfTheDayTranslationDTO getRandomGeneralFactOfTheDay();

    /**
     * Returns a random {@link FactOfTheDayTranslationDTO} for a user based on the
     * provided language code and the user's habit tags.
     *
     * @param email the email of the user for whom the tags are retrieved
     * @return a random fact of the day based on the user's habits
     */
    FactOfTheDayTranslationDTO getRandomFactOfTheDayForUser(String email);

    /**
     * Retrieves all tags associated with Facts of the Day.
     *
     * @return a set of {@link TagDto} representing all available tags for facts of
     *         the day
     */
    Set<TagDto> getAllFactOfTheDayTags();
}
