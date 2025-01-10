package greencity.service;

import greencity.constant.AppConstant;
import greencity.constant.ErrorMessage;
import greencity.dto.PageableHabitManagementDto;
import greencity.dto.filter.FilterHabitDto;
import greencity.dto.habit.HabitManagementDto;
import greencity.dto.habit.HabitVO;
import greencity.dto.habittranslation.HabitTranslationManagementDto;
import greencity.entity.Habit;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.WrongIdException;
import greencity.repository.HabitRepo;
import greencity.repository.HabitTranslationRepo;
import greencity.repository.UserActionRepo;
import greencity.repository.options.HabitFilter;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementation of {@link ManagementHabitService}.
 */
@Service
@AllArgsConstructor
public class ManagementHabitServiceImpl implements ManagementHabitService {
    private final HabitRepo habitRepo;
    private final HabitTranslationRepo habitTranslationRepo;
    private final LanguageService languageService;
    private final FileService fileService;
    private final HabitAssignService habitAssignService;
    private final UserActionRepo userActionRepo;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public HabitManagementDto getById(Long id) {
        Habit habit = habitRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + id));
        return modelMapper.map(habit, HabitManagementDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PageableHabitManagementDto<HabitManagementDto> getAllHabitsDto(String searchReg, Integer durationFrom,
        Integer durationTo, Integer complexity, Boolean withoutImage,
        Boolean withImage,
        Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.DESC, "id"));
        }

        if (withImage == null) {
            withImage = false;
        }
        if (withoutImage == null) {
            withoutImage = false;
        }
        FilterHabitDto filterHabitDto = new FilterHabitDto(searchReg,
            durationFrom,
            durationTo,
            complexity,
            withoutImage, withImage);
        Page<Habit> habits = habitRepo.findAll(new HabitFilter(filterHabitDto), pageable);
        List<HabitManagementDto> habitDtos = habits.getContent()
            .stream()
            .map(habit -> modelMapper.map(habit, HabitManagementDto.class))
            .toList();
        return new PageableHabitManagementDto<>(
            habitDtos,
            habits.getTotalElements(),
            habits.getPageable().getPageNumber(),
            habits.getTotalPages(),
            getSortModel(pageable));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public HabitManagementDto saveHabitAndTranslations(HabitManagementDto habitManagementDto, MultipartFile image) {
        Habit habit = buildHabitWithTranslations(habitManagementDto);
        uploadImageForHabit(habitManagementDto, image, habit);
        habit.setIsDeleted(false);
        habit.setIsCustomHabit(true);
        habitRepo.save(habit);
        habitTranslationRepo.saveAll(habit.getHabitTranslations());
        return modelMapper.map(habit, HabitManagementDto.class);
    }

    /**
     * Method builds {@link Habit} with {@link HabitManagementDto} fields.
     *
     * @param habitManagementDto {@link HabitManagementDto} instance.
     * @return {@link Habit}.
     */
    private Habit buildHabitWithTranslations(HabitManagementDto habitManagementDto) {
        Habit habit = Habit.builder()
            .complexity(habitManagementDto.getComplexity())
            .defaultDuration(habitManagementDto.getDefaultDuration())
            .habitTranslations(
                habitManagementDto.getHabitTranslations().stream()
                    .map(habitTranslationDto -> HabitTranslation.builder()
                        .description(habitTranslationDto.getDescription())
                        .habitItem(habitTranslationDto.getHabitItem())
                        .name(habitTranslationDto.getName())
                        .language(modelMapper.map(
                            languageService.findByCode(habitTranslationDto.getLanguageCode()),
                            Language.class))
                        .build())
                    .toList())
            .build();
        habit.getHabitTranslations().forEach(habitTranslation -> habitTranslation.setHabit(habit));
        return habit;
    }

    /**
     * Handles image assignment or uploading for the {@link Habit}.
     *
     * @param habitManagementDto {@link HabitManagementDto} instance.
     * @param image              {@link MultipartFile} image.
     * @param habit              {@link Habit} instance.
     */
    private void uploadImageForHabit(HabitManagementDto habitManagementDto, MultipartFile image, Habit habit) {
        if (image != null && !image.isEmpty()) {
            habit.setImage(fileService.upload(image));
        } else {
            habit.setImage(habitManagementDto.getImage() != null ? habitManagementDto.getImage()
                : AppConstant.DEFAULT_HABIT_IMAGE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void update(HabitManagementDto habitManagementDto, MultipartFile image) {
        Habit habit = habitRepo.findById(habitManagementDto.getId())
            .orElseThrow(() -> new WrongIdException(ErrorMessage.HABIT_NOT_FOUND_BY_ID));

        Map<String, HabitTranslationManagementDto> translationDtoMap = getMapTranslationsDtos(habitManagementDto);
        habit.getHabitTranslations().forEach(
            ht -> enhanceTranslationWithDto(translationDtoMap.get(ht.getLanguage().getCode()), ht));

        uploadImageForHabit(habitManagementDto, image, habit);
        habit.setComplexity(habitManagementDto.getComplexity());
        habit.setDefaultDuration(habitManagementDto.getDefaultDuration());
        habitRepo.save(habit);
    }

    /**
     * Method updates {@link HabitTranslation} with
     * {@link HabitTranslationManagementDto} fields.
     *
     * @param htDto {@link HabitTranslationManagementDto} instance.
     * @param ht    {@link HabitTranslation} instance.
     */
    private void enhanceTranslationWithDto(HabitTranslationManagementDto htDto, HabitTranslation ht) {
        ht.setDescription(htDto.getDescription());
        ht.setHabitItem(htDto.getHabitItem());
        ht.setName(htDto.getName());
    }

    /**
     * Method returns map with {@link HabitTranslationManagementDto} as a value and
     * it's {@link String} language code as a key.
     *
     * @param habitManagementDto {@link HabitManagementDto} instance.
     * @return {@link Map} with {@link String} key and
     *         {@link HabitTranslationManagementDto} instance value.
     */
    private Map<String, HabitTranslationManagementDto> getMapTranslationsDtos(HabitManagementDto habitManagementDto) {
        return habitManagementDto.getHabitTranslations().stream()
            .collect(Collectors.toMap(HabitTranslationManagementDto::getLanguageCode,
                Function.identity()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void delete(Long id) {
        Habit habit = habitRepo.findById(id)
            .orElseThrow(() -> new WrongIdException(ErrorMessage.HABIT_NOT_FOUND_BY_ID));
        HabitVO habitVO = modelMapper.map(habit, HabitVO.class);

        userActionRepo.deleteAllByHabitId(id);
        habitTranslationRepo.deleteAllByHabit(habit);
        habitAssignService.deleteAllHabitAssignsByHabit(habitVO);
        habitRepo.delete(habit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void deleteAll(List<Long> listId) {
        listId.forEach(this::delete);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Transactional
    public void switchIsDeletedStatus(Long id, Boolean newStatus) {
        Habit habit = habitRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + id));
        habit.setIsDeleted(newStatus);
        habitRepo.save(habit);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void switchIsCustomStatus(Long id, Boolean newIsCustomStatus) {
        Habit habit = habitRepo.findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + id));
        habit.setIsCustomHabit(newIsCustomStatus);
        habitRepo.save(habit);
    }

    private String getSortModel(Pageable pageable) {
        return pageable.getSort().stream()
            .map(order -> order.getProperty() + "," + order.getDirection())
            .collect(Collectors.joining(","));
    }
}
