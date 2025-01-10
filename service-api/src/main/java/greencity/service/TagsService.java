package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.tag.NewTagDto;
import greencity.dto.tag.TagDto;
import greencity.dto.tag.TagPostDto;
import greencity.dto.tag.TagVO;
import greencity.dto.tag.TagViewDto;
import greencity.enums.TagType;
import org.springframework.data.domain.Pageable;
import java.util.List;

/**
 * Provides the interface to manage Tag.
 *
 * @author Kovaliv Taras
 * @version 1.0
 */
public interface TagsService {
    /**
     * Method that returns all tags.
     *
     * @param pageable {@link Pageable}
     * @param filter   {@link String}
     * @return all tags {@link PageableAdvancedDto}
     */
    PageableAdvancedDto<TagVO> findAll(Pageable pageable, String filter);

    /**
     * Method that search tags by several values using Spring Data Specification.
     *
     * @param pageable   {@link Pageable}
     * @param tagViewDto {@link TagViewDto} - object that stores values of fields
     *                   that will be searched.
     * @return found tags {@link PageableAdvancedDto}
     */
    PageableAdvancedDto<TagVO> search(Pageable pageable, TagViewDto tagViewDto);

    /**
     * Method that saves new tag.
     *
     * @param tag - new tag {@link greencity.dto.tag.TagPostDto}
     * @return saved tag {@link TagVO}
     */
    TagVO save(TagPostDto tag);

    /**
     * Method that finds one tag by given id.
     *
     * @param id - {@link Long}
     * @return found tag {@link TagVO}
     */
    TagVO findById(Long id);

    /**
     * Method that deletes by given id.
     *
     * @param id - {@link Long}
     * @return id {@link Long} of deleted tag
     */
    Long deleteById(Long id);

    /**
     * Method that deletes all tags by given ids.
     *
     * @param ids - list of {@link Long}.
     * @return list of {@link Long} ids of deleted tags.
     */
    List<Long> bulkDelete(List<Long> ids);

    /**
     * Method that updates tag by given id.
     *
     * @param tagPostDto - {@link TagPostDto}
     * @param id         - {@link Long}
     * @return updated advice - {@link TagVO}
     */
    TagVO update(TagPostDto tagPostDto, Long id);

    /**
     * Method that allow you to find list of Tags by names.
     *
     * @param tags list of {@link String} values
     * @return list of Tags
     */
    List<TagVO> findTagsByNamesAndType(List<String> tags, TagType tagType);

    /**
     * Method that allow you to find list of Tags with all translations by names.
     *
     * @param tags list of {@link String} values
     * @return list of Tags
     */
    List<TagVO> findTagsWithAllTranslationsByNamesAndType(List<String> tags, TagType tagType);

    /**
     * Method that allow you to find list of Tags by type and language code.
     *
     * @param type         {@link TagType}
     * @param languageCode {@link String}
     * @return {@link List} of {@link TagDto}
     */
    List<TagDto> findByTypeAndLanguageCode(TagType type, String languageCode);

    /**
     * Method that allow you to find list of Tags by type.
     *
     * @param type {@link TagType}
     * @return {@link List} of {@link TagDto}
     */
    List<NewTagDto> findByType(TagType type);

    /**
     * Method that allow you to find all EcoNews Tags.
     *
     * @return list of Tag's names
     */
    List<TagDto> findAllEcoNewsTags(String languageCode);

    /**
     * Method that finds all Habits tags.
     *
     * @param languageCode {@link String}
     * @return list of tag names
     * @author Markiyan Derevetskyi
     */
    List<String> findAllHabitsTags(String languageCode);
}
