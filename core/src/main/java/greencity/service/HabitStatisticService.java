package greencity.service;

import greencity.dto.habitstatistic.*;
import greencity.entity.Habit;
import greencity.entity.HabitStatistic;
import greencity.entity.User;
import java.util.List;

public interface HabitStatisticService {
    /**
     * Method for creating {@link HabitStatistic} to database.
     *
     * @param addHabitStatisticDto - dto with {@link HabitStatistic} rate, amount of items, date and {@link Habit} id.
     * @return {@link AddHabitStatisticDto} instance.
     */
    AddHabitStatisticDto save(AddHabitStatisticDto addHabitStatisticDto);

    /**
     * Method for updating {@link HabitStatistic} in database.
     *
     * @param dto - dto with {@link HabitStatistic} id, rate and amount of items.
     * @return {@link UpdateHabitStatisticDto} instance.
     */
    UpdateHabitStatisticDto update(Long habitStatisticId, UpdateHabitStatisticDto dto);

    /**
     * Method for finding {@link HabitStatistic} by its id.
     *
     * @param id {@link HabitStatistic} id
     * @return a {@link HabitStatistic} instance
     */
    HabitStatistic findById(Long id);

    /**
     * Method for finding all {@link Habit} by {@link User} email.
     *
     * @param userId - {@link User} email.
     * @return list of user's {@link Habit}.
     */
    List<Habit> findAllHabitsByUserId(Long userId);

    /**
     * Method for finding all {@link HabitDto} by {@link User} email and {@link Habit}status.
     *
     * @param userId {@link User} id.
     * @param status {@link Habit} status.
     * @return list of {@link HabitDto}
     */
    List<Habit> findAllHabitsByStatus(Long userId, Boolean status);

    /**
     * Method for finding some statistics by {@link User} email.
     *
     * @param userId {@link User} id.
     * @return {@link CalendarUsefulHabitsDto} instance.
     */
    CalendarUsefulHabitsDto getInfoAboutUserHabits(Long userId);

    /**
     * Method for finding all {@link HabitStatisticDto} by {@link Habit id}.
     *
     * @param habitId {@link Habit} id.
     * @return list of {@link HabitStatisticDto} instances.
     */
    List<HabitStatisticDto> findAllByHabitId(Long habitId);

    /**
     * Method for finding all habits by status with statistic for each habit.
     *
     * @param id     user's id.
     * @param status status of habit.
     * @return list of {@link HabitDto} instances.
     */
    List<HabitDto> findAllHabitsAndTheirStatistics(Long id, Boolean status, String language);

    /**
     * Returns statistics for all not taken habit items in the system for today.
     * Data is returned as an array of key-value-pairs mapped to {@link HabitItemsAmountStatisticDto},
     * where key is the name of habit item and value is not taken amount of these items.
     * Language of habit items is defined by the `language` parameter.
     *
     * @param language - Name of habit item localization language(e.x. "en" or "uk").
     * @return {@link List} of {@link HabitItemsAmountStatisticDto}s contain those key-value pairs.
     */
    List<HabitItemsAmountStatisticDto> getTodayStatisticsForAllHabitItems(String language);

    /**
     * Method for getting amount of habits in progress by user id.
     *
     * @param id {@link Long} user id.
     * @return amount of habits in progress by user id.
     */
    Long getAmountOfHabitsInProgressByUserId(Long id);

    /**
     * Method for getting amount of acquired habits by user id.
     *
     * @param id {@link Long} user id.
     * @return amount of acquired habits by user id.
     */
    Long getAmountOfAcquiredHabitsByUserId(Long id);
}
