package greencity.service;

import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habitstatistic.AddHabitStatisticDto;
import greencity.dto.habitstatistic.HabitItemsAmountStatisticDto;
import greencity.dto.habitstatistic.HabitStatisticDto;
import greencity.dto.habitstatistic.UpdateHabitStatisticDto;
import java.util.List;

public interface HabitStatisticService {
    /**
     * Method for creating {@code HabitStatistic} by {@code Habit}, {@code User}
     * id's and {@link AddHabitStatisticDto} instance.
     *
     * @param habitId              {@code Habit} id.
     * @param userId               {@code User} id.
     * @param addHabitStatisticDto - dto with {@code HabitStatistic} rate, amount of
     *                             items, date.
     * @return {@link AddHabitStatisticDto} instance.
     */
    HabitStatisticDto saveByHabitIdAndUserId(Long habitId, Long userId, AddHabitStatisticDto addHabitStatisticDto);

    /**
     * Method for updating {@code HabitStatistic} by it's id.
     *
     * @param habitStatisticId {@code HabitStatistic} id.
     * @param userId           {@code User} current id.
     * @param dto              - dto with {@code HabitStatistic} rate and amount of
     *                         items.
     * @return {@link UpdateHabitStatisticDto} instance.
     */
    UpdateHabitStatisticDto update(Long habitStatisticId, Long userId, UpdateHabitStatisticDto dto);

    /**
     * Method for finding {@code HabitStatistic} by it's id.
     *
     * @param id HabitStatistic id.
     * @return {@link HabitStatisticDto} instance.
     */
    HabitStatisticDto findById(Long id);

    /**
     * Method for finding all {@code HabitStatistic} by HabitAssign id (multiple
     * statistics for one assigned habit for user).
     *
     * @param habitAssignId HabitAssign id.
     * @return list of {@link HabitStatisticDto} instances.
     */
    List<HabitStatisticDto> findAllStatsByHabitAssignId(Long habitAssignId);

    /**
     * Method for finding all {@code HabitStatistic} by Habit id.
     *
     * @param habitId Habit id.
     * @return list of {@link HabitStatisticDto} instances.
     */
    List<HabitStatisticDto> findAllStatsByHabitId(Long habitId);

    /**
     * Returns statistics for all not taken habit items in the system for today.
     * Data is returned as an array of key-value-pairs mapped to
     * {@link HabitItemsAmountStatisticDto}, where key is the name of habit item and
     * value is not taken amount of these items. Language of habit items is defined
     * by the `language` parameter.
     *
     * @param language - Name of habit item localization language(e.x. "en" or
     *                 "ua").
     * @return {@link List} of {@link HabitItemsAmountStatisticDto}s contain those
     *         key-value pairs.
     */
    List<HabitItemsAmountStatisticDto> getTodayStatisticsForAllHabitItems(String language);

    /**
     * Method for getting amount of habits in progress by user id.
     *
     * @param id {@code User} id.
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

    /**
     * Method for deleting all statistics for certain {@code HabitAssign}.
     *
     * @param habitAssign {HabitAssign} instance.
     */
    void deleteAllStatsByHabitAssign(HabitAssignVO habitAssign);
}
