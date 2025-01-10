package greencity.service;

import greencity.ModelUtils;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.tag.NewTagDto;
import greencity.dto.tag.TagDto;
import greencity.dto.tag.TagPostDto;
import greencity.dto.tag.TagVO;
import greencity.dto.tag.TagViewDto;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import greencity.enums.TagType;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.TagNotFoundException;
import greencity.filters.TagSpecification;
import greencity.repository.TagTranslationRepo;
import greencity.repository.TagsRepo;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class TagsServiceImplTest {
    @Mock
    private TagsRepo tagRepo;

    @Mock
    private TagTranslationRepo tagTranslationRepo;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private TagsServiceImpl tagsService;

    private static final String UKRAINIAN_LANGUAGE = "ua";
    private static final String ENGLISH_LANGUAGE = "en";

    @Test
    void findAll() {
        Pageable pageable = PageRequest.of(1, 8);
        List<Tag> tags = ModelUtils.getTags();
        Page<Tag> pageTags = new PageImpl<>(tags, pageable, 1);
        when(tagRepo.findAll(pageable)).thenReturn(pageTags);
        when(modelMapper.map(tags.getFirst(), TagVO.class)).thenReturn(ModelUtils.getTagVO());

        PageableAdvancedDto<TagVO> actual = ModelUtils.getPageableAdvancedDtoForTag();
        PageableAdvancedDto<TagVO> expected = tagsService.findAll(pageable, null);

        assertEquals(expected, actual);
    }

    @Test
    void findAllWithFilter() {
        String filter = "test";
        Pageable pageable = PageRequest.of(1, 8);
        List<Tag> tags = ModelUtils.getTags();
        Page<Tag> pageTags = new PageImpl<>(tags, pageable, 1);
        when(tagRepo.filterByAllFields(pageable, filter)).thenReturn(pageTags);
        when(modelMapper.map(tags.getFirst(), TagVO.class)).thenReturn(ModelUtils.getTagVO());

        PageableAdvancedDto<TagVO> actual = ModelUtils.getPageableAdvancedDtoForTag();
        PageableAdvancedDto<TagVO> expected = tagsService.findAll(pageable, filter);

        assertEquals(expected, actual);
    }

    @Test
    void search() {
        Pageable pageable = PageRequest.of(1, 8);
        TagViewDto tagViewDto = TagViewDto.builder()
            .id("3").name("News").type("ECO")
            .build();
        List<Tag> tags = ModelUtils.getTags();
        Page<Tag> pageTags = new PageImpl<>(tags, pageable, 1);
        when(tagRepo.findAll(any(TagSpecification.class), eq(pageable))).thenReturn(pageTags);
        when(modelMapper.map(tags.getFirst(), TagVO.class)).thenReturn(ModelUtils.getTagVO());

        PageableAdvancedDto<TagVO> actual = ModelUtils.getPageableAdvancedDtoForTag();
        PageableAdvancedDto<TagVO> expected = tagsService.search(pageable, tagViewDto);

        assertEquals(expected, actual);
    }

    @Test
    void searchByOneField() {
        Pageable pageable = PageRequest.of(1, 8);
        TagViewDto tagViewDto = TagViewDto.builder()
            .name("News").type("ECO")
            .build();
        List<Tag> tags = ModelUtils.getTags();
        Page<Tag> pageTags = new PageImpl<>(tags, pageable, 1);
        when(tagRepo.findAll(any(TagSpecification.class), eq(pageable))).thenReturn(pageTags);
        when(modelMapper.map(tags.getFirst(), TagVO.class)).thenReturn(ModelUtils.getTagVO());

        PageableAdvancedDto<TagVO> actual = ModelUtils.getPageableAdvancedDtoForTag();
        PageableAdvancedDto<TagVO> expected = tagsService.search(pageable, tagViewDto);

        assertEquals(expected, actual);
    }

    @Test
    void save() {
        TagPostDto tagPostDto = ModelUtils.getTagPostDto();
        Tag toSave = ModelUtils.getTag();
        Tag saved = ModelUtils.getTag();
        saved.setId(1L);
        TagVO actual = ModelUtils.getTagVO();

        when(modelMapper.map(tagPostDto, Tag.class)).thenReturn(toSave);
        when(tagRepo.save(toSave)).thenReturn(saved);
        when(modelMapper.map(saved, TagVO.class)).thenReturn(actual);

        TagVO expected = tagsService.save(tagPostDto);

        assertEquals(expected, actual);
    }

    @Test
    void findById() {
        Long id = 1L;
        Tag tag = ModelUtils.getTag();
        TagVO expected = ModelUtils.getTagVO();

        when(tagRepo.findById(id)).thenReturn(Optional.of(tag));
        when(modelMapper.map(tag, TagVO.class)).thenReturn(expected);
        TagVO actual = tagsService.findById(id);

        assertEquals(expected, actual);
    }

    @Test
    void findByIdThrowNotFoundException() {
        assertThrows(NotFoundException.class, () -> tagsService.findById(3L));
    }

    @Test
    void update() {
        Long id = 1L;
        Tag toUpdate = ModelUtils.getTag();
        TagPostDto tagPostDto = ModelUtils.getTagPostDto();
        TagVO expected = ModelUtils.getTagVO();

        when(tagRepo.findById(id)).thenReturn(Optional.of(toUpdate));
        when(modelMapper.map(toUpdate, TagVO.class)).thenReturn(expected);
        TagVO actual = tagsService.update(tagPostDto, id);

        assertEquals(expected, actual);
    }

    @Test
    void updateThrowNotFoundException() {
        Long id = 1L;
        TagPostDto tagPostDto = ModelUtils.getTagPostDto();
        assertThrows(NotFoundException.class, () -> tagsService.update(tagPostDto, id));
    }

    @Test
    void deleteById() {
        Long id = 1L;

        Long actual = tagsService.deleteById(id);

        verify(tagRepo).deleteById(id);
        assertEquals(id, actual);
    }

    @Test
    void deleteByIdThrowNotDeletedException() {
        Long id = 3L;

        doThrow(EmptyResultDataAccessException.class).when(tagRepo).deleteById(id);
        assertThrows(NotDeletedException.class, () -> tagsService.deleteById(id));
    }

    @Test
    void bulkDelete() {
        List<Long> ids = Arrays.asList(1L, 2L, 3L);

        List<Long> actual = tagsService.bulkDelete(ids);

        verify(tagTranslationRepo).bulkDeleteByTagId(ids);
        verify(tagRepo).bulkDelete(ids);
        assertEquals(ids, actual);
    }

    @Test
    void findTagsByNames() {
        TagType tagType = TagType.ECO_NEWS;
        List<String> tagsNames = Collections.singletonList("News");
        List<String> lowerTagsNames = tagsNames.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        List<Tag> ecoNewsTags = ModelUtils.getTags();
        List<TagVO> actual = Collections.singletonList(ModelUtils.getTagVO());
        when(tagRepo.findTagsByNamesAndType(lowerTagsNames, tagType)).thenReturn(ecoNewsTags);
        when(modelMapper.map(ecoNewsTags, new TypeToken<List<TagVO>>() {
        }.getType())).thenReturn(actual);
        List<TagVO> expected = tagsService.findTagsByNamesAndType(tagsNames, tagType);

        assertEquals(expected, actual);
    }

    @Test
    void findByTypeAndLanguageCode() {
        TagType tagType = TagType.ECO_NEWS;
        String languageCode = "en";
        TagDto tagDto = ModelUtils.getTagDto();
        List<TagDto> actual = Collections.singletonList(tagDto);
        List<TagTranslation> tagTranslations = Collections.singletonList(ModelUtils.getTagTranslations().get(1));

        when(tagRepo.findTagsByTypeAndLanguageCode(tagType, languageCode))
            .thenReturn(tagTranslations);
        when(modelMapper.map(tagTranslations, new TypeToken<List<TagDto>>() {
        }.getType())).thenReturn(actual);
        List<TagDto> expected = tagsService.findByTypeAndLanguageCode(tagType, languageCode);

        assertEquals(expected, actual);
    }

    @Test
    void findTagsByNamesThrowTagNotFoundException() {
        TagType tagType = TagType.ECO_NEWS;
        List<String> tagsNames = Collections.singletonList("News");
        List<String> lowerTagsNames = tagsNames.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        List<Tag> tags = Collections.emptyList();
        when(tagRepo.findTagsByNamesAndType(lowerTagsNames, tagType)).thenReturn(tags);

        assertThrows(TagNotFoundException.class,
            () -> tagsService.findTagsByNamesAndType(tagsNames, tagType));
    }

    @Test
    void findAllEcoNewsTags() {
        List<TagTranslation> tagTranslations = Collections.singletonList(ModelUtils.getTagTranslations().get(1));
        List<TagDto> actual = Collections.singletonList(TagDto.builder().id(1L).name("News").build());
        when(tagTranslationRepo.findAllEcoNewsTags(ENGLISH_LANGUAGE)).thenReturn(tagTranslations);
        when(modelMapper.map(tagTranslations, new TypeToken<List<TagDto>>() {
        }.getType())).thenReturn(actual);
        List<TagDto> expected = tagsService.findAllEcoNewsTags(ENGLISH_LANGUAGE);

        assertEquals(expected, actual);
    }

    @Test
    void findAllHabitsTags() {
        List<String> actual = Collections.singletonList("Новини");
        when(tagRepo.findAllHabitsTags(UKRAINIAN_LANGUAGE)).thenReturn(actual);
        List<String> expected = tagsService.findAllHabitsTags(UKRAINIAN_LANGUAGE);

        assertEquals(expected, actual);
    }

    @Test
    void findByTypeTest() {
        List<NewTagDto> tags = List.of(NewTagDto.builder().id(1L).name("News").nameUa("Новини").build());

        when(tagRepo.findTagsByType(TagType.ECO_NEWS)).thenReturn(ModelUtils.getTags());
        when(modelMapper.map(ModelUtils.getTags(), new TypeToken<List<NewTagDto>>() {
        }.getType())).thenReturn(tags);

        assertEquals(tags, tagsService.findByType(TagType.ECO_NEWS));

        verify(tagRepo).findTagsByType(TagType.ECO_NEWS);
        verify(modelMapper).map(ModelUtils.getTags(), new TypeToken<List<NewTagDto>>() {
        }.getType());
    }
}
