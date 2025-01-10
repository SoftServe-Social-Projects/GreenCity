package greencity.service;

import greencity.constant.CacheConstants;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.factoftheday.FactOfTheDayDTO;
import greencity.dto.factoftheday.FactOfTheDayPostDTO;
import greencity.dto.factoftheday.FactOfTheDayTranslationDTO;
import greencity.dto.factoftheday.FactOfTheDayTranslationVO;
import greencity.dto.tag.TagDto;
import greencity.entity.FactOfTheDay;
import greencity.entity.FactOfTheDayTranslation;
import greencity.entity.Language;
import greencity.entity.Tag;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotUpdatedException;
import greencity.repository.FactOfTheDayRepo;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import greencity.repository.TagsRepo;
import jakarta.annotation.Resource;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import static greencity.enums.TagType.FACT_OF_THE_DAY;

/**
 * Implementation of {@link FactOfTheDayService}.
 */
@AllArgsConstructor
@Service
@Scope(proxyMode = ScopedProxyMode.INTERFACES)
public class FactOfTheDayServiceImpl implements FactOfTheDayService {
    private final FactOfTheDayRepo factOfTheDayRepo;
    private final ModelMapper modelMapper;
    private final LanguageService languageService;
    private final FactOfTheDayTranslationService factOfTheDayTranslationService;
    private final TagsRepo tagsRepo;
    @Resource
    private FactOfTheDayService self;

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<FactOfTheDayDTO> getAllFactsOfTheDay(Pageable pageable) {
        Page<FactOfTheDay> factsOfTheDay;
        List<FactOfTheDayDTO> factOfTheDayDTOs;
        try {
            factsOfTheDay = factOfTheDayRepo.findAll(pageable);
            factOfTheDayDTOs =
                factsOfTheDay.getContent().stream()
                    .map(factOfTheDay -> modelMapper.map(factOfTheDay, FactOfTheDayDTO.class))
                    .map(factOfTheDay -> factOfTheDay
                        .setTags(factOfTheDayRepo.findTagsByFactOfTheDayId(factOfTheDay.getId())))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new NotFoundException(ErrorMessage.FACT_OF_THE_DAY_PROPERTY_NOT_FOUND + pageable.getSort());
        }
        return new PageableDto<>(
            factOfTheDayDTOs,
            factsOfTheDay.getTotalElements(),
            factsOfTheDay.getPageable().getPageNumber(),
            factsOfTheDay.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FactOfTheDayPostDTO saveFactOfTheDayAndTranslations(FactOfTheDayPostDTO factPost) {
        FactOfTheDay factOfTheDay = FactOfTheDay.builder()
            .name(factPost.getName())
            .factOfTheDayTranslations(
                factPost.getFactOfTheDayTranslations().stream()
                    .map(el -> FactOfTheDayTranslation.builder()
                        .content(el.getContent())
                        .language(modelMapper.map(languageService.findByCode(el.getLanguageCode()), Language.class))
                        .build())
                    .collect(Collectors.toList()))
            .tags(tagsRepo.findTagsById(factPost.getTags()))
            .build();
        factOfTheDay.getFactOfTheDayTranslations().forEach(el -> el.setFactOfTheDay(factOfTheDay));
        factOfTheDayRepo.save(factOfTheDay);
        List<FactOfTheDayTranslationVO> map = factOfTheDay.getFactOfTheDayTranslations()
            .stream()
            .map(fact -> modelMapper.map(fact, FactOfTheDayTranslationVO.class))
            .collect(Collectors.toList());
        factOfTheDayTranslationService.saveAll(map);
        return factPost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FactOfTheDayPostDTO updateFactOfTheDayAndTranslations(FactOfTheDayPostDTO factPost) {
        FactOfTheDay factOfTheDayFromDB =
            factOfTheDayRepo.findById(factPost.getId())
                .orElseThrow(() -> new NotUpdatedException(ErrorMessage.FACT_OF_THE_DAY_NOT_UPDATED));
        List<FactOfTheDayTranslationVO> factOfTheDayTranslationVOList = factOfTheDayFromDB.getFactOfTheDayTranslations()
            .stream()
            .map(fact -> modelMapper.map(fact, FactOfTheDayTranslationVO.class))
            .collect(Collectors.toList());
        factOfTheDayTranslationService.deleteAll(factOfTheDayTranslationVOList);
        FactOfTheDay factOfTheDay = FactOfTheDay.builder()
            .id(factPost.getId())
            .name(factPost.getName())
            .factOfTheDayTranslations(
                factPost.getFactOfTheDayTranslations().stream()
                    .map(el -> FactOfTheDayTranslation.builder()
                        .content(el.getContent())
                        .language(modelMapper.map(languageService.findByCode(el.getLanguageCode()), Language.class))
                        .build())
                    .collect(Collectors.toList()))
            .createDate(ZonedDateTime.now())
            .tags(tagsRepo.findTagsById(factPost.getTags()))
            .build();
        factOfTheDay.getFactOfTheDayTranslations().forEach(el -> el.setFactOfTheDay(factOfTheDay));
        factOfTheDayRepo.save(factOfTheDay);
        List<FactOfTheDayTranslationVO> factOfTheDayTranslationVOS = factOfTheDay.getFactOfTheDayTranslations()
            .stream()
            .map(fact -> modelMapper.map(fact, FactOfTheDayTranslationVO.class))
            .collect(Collectors.toList());
        factOfTheDayTranslationService.saveAll(factOfTheDayTranslationVOS);
        return factPost;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long deleteFactOfTheDayAndTranslations(Long id) {
        deleteAllFactOfTheDayAndTranslations(List.of(id));
        return id;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<Long> deleteAllFactOfTheDayAndTranslations(List<Long> listId) {
        listId.forEach(id -> {
            FactOfTheDay factOfTheDay = factOfTheDayRepo.findById(id)
                .orElseThrow(() -> new NotUpdatedException(ErrorMessage.FACT_OF_THE_DAY_NOT_DELETED));
            factOfTheDayRepo.deleteById(id);
            List<FactOfTheDayTranslationVO> factOfTheDayTranslationVOS = factOfTheDay.getFactOfTheDayTranslations()
                .stream()
                .map(fact -> modelMapper.map(fact, FactOfTheDayTranslationVO.class))
                .collect(Collectors.toList());
            factOfTheDayTranslationService.deleteAll(factOfTheDayTranslationVOS);
        });
        return listId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableDto<FactOfTheDayDTO> searchBy(Pageable pageable, String searchQuery) {
        Page<FactOfTheDay> factsOfTheDay = factOfTheDayRepo.searchBy(pageable, searchQuery);
        List<FactOfTheDayDTO> factOfTheDayDTOs = factsOfTheDay.getContent().stream()
            .map(factOfTheDay -> modelMapper.map(factOfTheDay, FactOfTheDayDTO.class))
            .map(factOfTheDay -> factOfTheDay
                .setTags(factOfTheDayRepo.findTagsByFactOfTheDayId(factOfTheDay.getId())))
            .collect(Collectors.toList());
        return new PageableDto<>(
            factOfTheDayDTOs,
            factsOfTheDay.getTotalElements(),
            factsOfTheDay.getPageable().getPageNumber(),
            factsOfTheDay.getTotalPages());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Cacheable(value = CacheConstants.FACT_OF_THE_DAY_CACHE_NAME)
    public FactOfTheDayTranslationDTO getRandomFactOfTheDayByTags(Set<Long> tagIds) {
        FactOfTheDay factOfTheDay = factOfTheDayRepo.getRandomFactOfTheDay(tagIds)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.FACT_OF_THE_DAY_NOT_FOUND));
        return modelMapper.map(factOfTheDay, FactOfTheDayTranslationDTO.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FactOfTheDayTranslationDTO getRandomGeneralFactOfTheDay() {
        List<Tag> tags = tagsRepo.findTagsByType(FACT_OF_THE_DAY);
        return getRandomFactOfTheDayByTags(tags.stream().map(Tag::getId).collect(Collectors.toSet()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FactOfTheDayTranslationDTO getRandomFactOfTheDayForUser(String userEmail) {
        Set<Long> userTagIds = tagsRepo.findTagsIdByUserHabitsInProgress(userEmail);
        try {
            return getRandomFactOfTheDayByTags(userTagIds);
        } catch (NotFoundException e) {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Set<TagDto> getAllFactOfTheDayTags() {
        return factOfTheDayRepo.findAllFactOfTheDayAndHabitTags();
    }
}
