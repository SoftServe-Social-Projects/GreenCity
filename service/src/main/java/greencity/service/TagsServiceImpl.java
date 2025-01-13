package greencity.service;

import greencity.dto.PageableAdvancedDto;
import greencity.dto.tag.NewTagDto;
import greencity.dto.tag.TagDto;
import greencity.dto.tag.TagPostDto;
import greencity.dto.tag.TagTranslationDto;
import greencity.dto.tag.TagVO;
import greencity.dto.tag.TagViewDto;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import greencity.enums.TagType;
import greencity.exception.exceptions.*;
import greencity.filters.SearchCriteria;
import greencity.filters.TagSpecification;
import greencity.repository.TagTranslationRepo;
import greencity.repository.TagsRepo;
import greencity.constant.ErrorMessage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class TagsServiceImpl implements TagsService {
    private final TagsRepo tagRepo;
    private final TagTranslationRepo tagTranslationRepo;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<TagVO> findAll(Pageable pageable, String filter) {
        Page<Tag> tags = filter == null ? tagRepo.findAll(pageable)
            : tagRepo.filterByAllFields(pageable, filter);

        return buildPageableAdvanceDtoFromPage(tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableAdvancedDto<TagVO> search(Pageable pageable, TagViewDto tagViewDto) {
        Page<Tag> foundTags = tagRepo.findAll(buildSpecification(tagViewDto), pageable);

        return buildPageableAdvanceDtoFromPage(foundTags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagVO save(TagPostDto tag) {
        Tag toSave = modelMapper.map(tag, Tag.class);
        toSave.getTagTranslations().forEach(t -> t.setTag(toSave));
        Tag saved = tagRepo.save(toSave);

        return modelMapper.map(saved, TagVO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TagVO findById(Long id) {
        Tag foundTag = tagRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.TAG_NOT_FOUND + id));

        return modelMapper.map(foundTag, TagVO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long deleteById(Long id) {
        try {
            tagRepo.deleteById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new NotDeletedException(ErrorMessage.TAG_NOT_DELETED + id);
        }

        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public List<Long> bulkDelete(List<Long> ids) {
        tagTranslationRepo.bulkDeleteByTagId(ids);
        tagRepo.bulkDelete(ids);
        return ids;
    }

    @Override
    @Transactional
    public TagVO update(TagPostDto tagPostDto, Long id) {
        Tag toUpdate = tagRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.TAG_NOT_FOUND + id));
        enhanceTagWithNewData(toUpdate, tagPostDto);

        return modelMapper.map(toUpdate, TagVO.class);
    }

    private void enhanceTagWithNewData(Tag toUpdate, TagPostDto tagPostDto) {
        toUpdate.setType(tagPostDto.getType());
        toUpdate.getTagTranslations()
            .forEach(tagTranslation -> {
                Optional<TagTranslationDto> tagTranslationDto = tagPostDto.getTagTranslations().stream()
                    .filter(newTranslation -> newTranslation.getLanguage().getId()
                        .equals(tagTranslation.getLanguage().getId()))
                    .findFirst();
                tagTranslation.setName(tagTranslationDto.map(TagTranslationDto::getName).orElse(null));
            });
    }

    private PageableAdvancedDto<TagVO> buildPageableAdvanceDtoFromPage(Page<Tag> pageTags) {
        List<TagVO> tagVOs = pageTags.getContent().stream()
            .map(t -> modelMapper.map(t, TagVO.class))
            .collect(Collectors.toList());

        return new PageableAdvancedDto<>(
            tagVOs,
            pageTags.getTotalElements(), pageTags.getPageable().getPageNumber(),
            pageTags.getTotalPages(), pageTags.getNumber(),
            pageTags.hasPrevious(), pageTags.hasNext(),
            pageTags.isFirst(), pageTags.isLast());
    }

    private List<SearchCriteria> buildSearchCriteriaList(TagViewDto tagViewDto) {
        List<SearchCriteria> searchCriteriaList = new ArrayList<>();

        setValueIfNotEmpty(searchCriteriaList, "id", tagViewDto.getId());
        setValueIfNotEmpty(searchCriteriaList, "type", tagViewDto.getType());
        setValueIfNotEmpty(searchCriteriaList, "name", tagViewDto.getName());

        return searchCriteriaList;
    }

    private void setValueIfNotEmpty(List<SearchCriteria> searchCriteria, String key, String value) {
        if (StringUtils.hasLength(value)) {
            searchCriteria.add(SearchCriteria.builder()
                .key(key)
                .type(key)
                .value(value)
                .build());
        }
    }

    private TagSpecification buildSpecification(TagViewDto tagViewDto) {
        List<SearchCriteria> searchCriteriaList = buildSearchCriteriaList(tagViewDto);

        return new TagSpecification(searchCriteriaList);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TagVO> findTagsByNamesAndType(List<String> tagNames, TagType tagType) {
        List<String> lowerCaseTagNames = tagNames.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        List<Tag> tags = tagRepo.findTagsByNamesAndType(lowerCaseTagNames, tagType);
        if (tags.isEmpty()) {
            throw new TagNotFoundException(ErrorMessage.TAGS_NOT_FOUND);
        }
        return modelMapper.map(tags, new TypeToken<List<TagVO>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TagDto> findByTypeAndLanguageCode(TagType type, String languageCode) {
        List<TagTranslation> tagTranslations = tagRepo.findTagsByTypeAndLanguageCode(type, languageCode);

        return modelMapper.map(tagTranslations, new TypeToken<List<TagDto>>() {
        }.getType());
    }

    @Override
    public List<NewTagDto> findByType(TagType type) {
        List<Tag> tags = tagRepo.findTagsByType(type);

        return modelMapper.map(tags, new TypeToken<List<NewTagDto>>() {
        }.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TagDto> findAllEcoNewsTags(String languageCode) {
        List<TagTranslation> tagTranslations = tagTranslationRepo.findAllEcoNewsTags(languageCode);

        return modelMapper.map(tagTranslations, new TypeToken<List<TagDto>>() {
        }.getType());
    }

    @Override
    public List<String> findAllHabitsTags(String languageCode) {
        return tagRepo.findAllHabitsTags(languageCode);
    }

    @Override
    public List<TagVO> findTagsWithAllTranslationsByNamesAndType(List<String> tagNames, TagType tagType) {
        List<String> lowerCaseTagNames = tagNames.stream()
            .map(String::toLowerCase)
            .collect(Collectors.toList());
        List<Tag> tags = tagRepo.findAllByTagTranslations(lowerCaseTagNames, tagType);
        if (tags.isEmpty()) {
            throw new TagNotFoundException(ErrorMessage.TAGS_NOT_FOUND);
        }
        return modelMapper.map(tags, new TypeToken<List<TagVO>>() {
        }.getType());
    }
}