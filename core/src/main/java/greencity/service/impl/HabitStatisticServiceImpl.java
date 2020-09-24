package greencity.service.impl;

import greencity.constant.CacheConstants;
import greencity.constant.ErrorMessage;
import greencity.converters.DateService;
import greencity.dto.habitstatistic.*;
import greencity.dto.user.HabitDictionaryDto;
import greencity.dto.user.HabitLogItemDto;
import greencity.entity.Habit;
import greencity.entity.HabitDictionaryTranslation;
import greencity.entity.HabitStatistic;
import greencity.entity.enums.HabitRate;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotSavedException;
import greencity.repository.HabitRepo;
import greencity.repository.HabitStatisticRepo;
import greencity.service.HabitService;
import greencity.service.HabitStatisticService;
import java.time.LocalDate;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@EnableCaching
public class HabitStatisticServiceImpl implements HabitStatisticService {
    private final HabitStatisticRepo habitStatisticRepo;
    private final HabitRepo habitRepo;
    private final HabitService habitService;
    private final ModelMapper modelMapper;
    private final DateService dateService;

    /**
     * Constructor with parameters.
     */
    @Autowired
    public HabitStatisticServiceImpl(HabitStatisticRepo habitStatisticRepo,
                                     HabitRepo habitRepo,
                                     HabitService habitService, ModelMapper modelMapper,
                                     DateService dateService) {
        this.habitStatisticRepo = habitStatisticRepo;
        this.habitRepo = habitRepo;
        this.habitService = habitService;
        this.modelMapper = modelMapper;
        this.dateService = dateService;
    }

    /**
     * {@inheritDoc}
     *
     * @author Yuriy Olkhovskyi && Yurii Koval
     */
    @Transactional
    @CacheEvict(value = CacheConstants.HABIT_ITEM_STATISTIC_CACHE, allEntries = true)
    @Override
    public AddHabitStatisticDto save(AddHabitStatisticDto dto) {
        if (habitStatisticRepo.findHabitStatByDate(dto.getCreatedOn(), dto.getHabitId()).isPresent()) {
            throw new NotSavedException(ErrorMessage.HABIT_STATISTIC_ALREADY_EXISTS);
        }
        boolean proceed = isTodayOrYesterday(
            dateService
                .convertToDatasourceTimezone(dto.getCreatedOn())
                .toLocalDate()
        );
        if (proceed) {
            HabitStatistic habitStatistic = modelMapper.map(dto, HabitStatistic.class);
            habitStatistic.setHabit(habitService.getById(dto.getHabitId()));
            return modelMapper.map(habitStatisticRepo.save(habitStatistic), AddHabitStatisticDto.class);
        }
        throw new BadRequestException(ErrorMessage.WRONG_DATE);
    }

    private boolean isTodayOrYesterday(LocalDate date) {
        int diff = Period.between(LocalDate.now(), date).getDays();
        return diff == 0 || diff == -1;
    }


    /**
     * {@inheritDoc}
     *
     * @author Yuriy Olkhovskyi
     */
    @Transactional
    @CacheEvict(value = CacheConstants.HABIT_ITEM_STATISTIC_CACHE, allEntries = true)
    @Override
    public UpdateHabitStatisticDto update(Long habitStatisticId, UpdateHabitStatisticDto dto) {
        HabitStatistic updatable = findById(habitStatisticId);

        updatable.setAmountOfItems(dto.getAmountOfItems());
        updatable.setHabitRate(dto.getHabitRate());
        return modelMapper.map(habitStatisticRepo.save(updatable),
            UpdateHabitStatisticDto.class);
    }

    /**
     * {@inheritDoc}
     *
     * @author Yuriy Olkhovskyi
     */
    @Override
    public HabitStatistic findById(Long id) {
        return habitStatisticRepo
            .findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage
                .HABIT_STATISTIC_NOT_FOUND_BY_ID + id));
    }

    /**
     * {@inheritDoc}
     *
     * @author Yuriy Olkhovskyi
     */
    @Override
    public List<Habit> findAllHabitsByUserId(Long userId) {
        return habitRepo.findAllByUserId(userId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_HAS_NOT_ANY_HABITS));
    }

    /**
     * {@inheritDoc}
     *
     * @author Yuriy Olkhovskyi
     */
    @Override
    public List<Habit> findAllHabitsByStatus(Long userId, Boolean status) {
        List<Habit> habitList = findAllHabitsByUserId(userId)
            .stream()
            .filter(habit -> habit.getStatusHabit().equals(status))
            .collect(Collectors.toList());
        if (habitList.isEmpty()) {
            throw new NotFoundException(ErrorMessage.USER_HAS_NOT_HABITS_WITH_SUCH_STATUS + status);
        }
        return habitList;
    }

    /**
     * {@inheritDoc}
     *
     * @author Yuriy Olkhovskyi
     */
    @Override
    public List<HabitDto> findAllHabitsAndTheirStatistics(Long id, Boolean status, String language) {
        return findAllHabitsByStatus(id, status)
            .stream()
            .map(habit -> convertHabitToHabitDto(habit, language))
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * @author Yuriy Olkhovskyi
     */
    @Override
    public CalendarUsefulHabitsDto getInfoAboutUserHabits(Long userId) {
        List<Habit> allHabitsByUserId = findAllHabitsByStatus(userId, true);

        List<HabitLogItemDto> statisticByHabitsPerMonth = getAmountOfUnTakenItemsPerMonth(allHabitsByUserId);

        List<HabitLogItemDto> statisticUnTakenItemsWithPrevMonth =
            getDifferenceItemsWithPrevDay(allHabitsByUserId);

        CalendarUsefulHabitsDto dto = new CalendarUsefulHabitsDto();
        dto.setCreationDate(allHabitsByUserId.get(0).getCreateDate());
        dto.setAllItemsPerMonth(statisticByHabitsPerMonth);
        dto.setDifferenceUnTakenItemsWithPreviousDay(statisticUnTakenItemsWithPrevMonth);

        return dto;
    }

    private Integer getItemsForPreviousDay(Long habitId) {
        return habitStatisticRepo.getAmountOfItemsInPreviousDay(habitId).orElse(0);
    }

    private Integer getItemsTakenToday(Long habitId) {
        return habitStatisticRepo.getAmountOfItemsToday(habitId).orElse(0);
    }

    private List<HabitLogItemDto> getAmountOfUnTakenItemsPerMonth(List<Habit> allHabitsByUserId) {
        ZonedDateTime firstDayOfMonth = dateService.getDatasourceZonedDateTime();
        return allHabitsByUserId
            .stream()
            .map(habit -> new HabitLogItemDto(
                habit.getHabitDictionary().getImage(),
                habitStatisticRepo
                    .getSumOfAllItemsPerMonth(habit.getId(),
                        firstDayOfMonth.withDayOfMonth(1)).orElse(0))).collect(Collectors.toList());
    }

    private List<HabitLogItemDto> getDifferenceItemsWithPrevDay(List<Habit> allHabitsByUserId) {
        return allHabitsByUserId
            .stream()
            .map(habit -> new HabitLogItemDto(
                habit.getHabitDictionary().getImage(),
                getItemsTakenToday(habit.getId()) - getItemsForPreviousDay(habit.getId())
            )).collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     *
     * @author Yuriy Olkovskyi
     */
    @Override
    public List<HabitStatisticDto> findAllByHabitId(Long habitId) {
        return habitStatisticRepo.findAllByHabitId(habitId)
            .stream()
            .map(HabitStatisticDto::new)
            .collect(Collectors.toList());
    }

    private HabitDto convertHabitToHabitDto(Habit habit, String language) {
        List<HabitStatisticDto> result = new ArrayList<>();
        List<HabitStatistic> habitStatistics = habit.getHabitStatistics();
        ZonedDateTime zonedDateTime = habit.getCreateDate();
        int counter = 0;

        habitStatistics.sort(Comparator.comparing(HabitStatistic::getCreatedOn));

        for (int i = 0; i < 21; i++) {
            if (counter < habitStatistics.size()
                && zonedDateTime.toLocalDate().equals(habitStatistics.get(counter).getCreatedOn().toLocalDate())) {
                result.add(new HabitStatisticDto(habit.getHabitStatistics().get(counter)));
                counter++;
            } else {
                result.add(new HabitStatisticDto(null, HabitRate.DEFAULT, zonedDateTime, 0));
            }
            zonedDateTime = zonedDateTime.plusDays(1);
        }
        HabitDictionaryDto habitDictionaryDto = modelMapper.map(habit.getHabitDictionary(), HabitDictionaryDto.class);
        HabitDictionaryTranslation habitDictionaryTranslation = createHabitDictionaryTranslation(habit,
            habitDictionaryDto, language);

        return new HabitDto(habit.getId(),
            habitDictionaryTranslation.getName(),
            habit.getStatusHabit(),
            habitDictionaryTranslation.getDescription(),
            habitDictionaryTranslation.getName(),
            habitDictionaryTranslation.getHabitItem(),
            habit.getCreateDate(),
            result,
            habitDictionaryDto
        );
    }

    /**
     * Create HabitDictionaryTranslation.
     *
     * @param habit              {@link Habit}.
     * @param habitDictionaryDto {@link HabitDictionaryDto}.
     * @param language           language code.
     * @return {@link HabitDictionaryTranslation}.
     */
    private HabitDictionaryTranslation createHabitDictionaryTranslation(
        Habit habit, HabitDictionaryDto habitDictionaryDto, String language) {
        HabitDictionaryTranslation habitDictionaryTranslation = habit.getHabitDictionary()
            .getHabitDictionaryTranslations().stream()
            .filter(t -> t.getLanguage().getCode().equals(language))
            .findFirst().orElseThrow(() -> new NotFoundException("This habit doesn't exist for this language"));
        habitDictionaryDto.setDescription(habitDictionaryTranslation.getDescription());
        habitDictionaryDto.setHabitItem(habitDictionaryTranslation.getHabitItem());
        habitDictionaryDto.setName(habitDictionaryTranslation.getName());
        return habitDictionaryTranslation;
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = CacheConstants.HABIT_ITEM_STATISTIC_CACHE, key = "#language")
    @Override
    public List<HabitItemsAmountStatisticDto> getTodayStatisticsForAllHabitItems(String language) {
        return habitStatisticRepo.getStatisticsForAllHabitItemsByDate(ZonedDateTime.now(), language).stream()
            .map(it ->
                HabitItemsAmountStatisticDto.builder()
                    .habitItem((String) it.get(0))
                    .notTakenItems((long) it.get(1))
                    .build()
            ).collect(Collectors.toList());
    }

    /**
     * Method for getting amount of habits in progress by user id.
     *
     * @param id {@link Long} user id.
     * @return amount of habits in progress by user id.
     * @author Marian Datsko
     */
    @Override
    public Long getAmountOfHabitsInProgressByUserId(Long id) {
        return habitStatisticRepo.getAmountOfHabitsInProgressByUserId(id);
    }

    /**
     * Method for getting amount of acquired habits by user id.
     *
     * @param id {@link Long} user id.
     * @return amount of acquired habits by user id.
     * @author Marian Datsko
     */
    @Override
    public Long getAmountOfAcquiredHabitsByUserId(Long id) {
        return habitStatisticRepo.getAmountOfAcquiredHabitsByUserId(id);
    }
}
