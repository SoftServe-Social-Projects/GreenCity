package greencity.service;

import greencity.constant.CacheConstants;
import greencity.constant.ErrorMessage;
import greencity.converters.DateService;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habitstatistic.HabitDateCount;
import greencity.dto.habitstatistic.HabitStatusCount;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatistic;
import greencity.enums.HabitAssignStatus;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotSavedException;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitStatisticRepo;
import greencity.dto.habitstatistic.AddHabitStatisticDto;
import greencity.dto.habitstatistic.HabitStatisticDto;
import greencity.dto.habitstatistic.UpdateHabitStatisticDto;
import greencity.dto.habitstatistic.GetHabitStatisticDto;
import greencity.dto.habitstatistic.HabitItemsAmountStatisticDto;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Period;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import greencity.repository.UserRepo;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@EnableCaching
@AllArgsConstructor
public class HabitStatisticServiceImpl implements HabitStatisticService {
    private final HabitStatisticRepo habitStatisticRepo;
    private final HabitAssignRepo habitAssignRepo;
    private final HabitRepo habitRepo;
    private final DateService dateService;
    private final ModelMapper modelMapper;
    private final UserRepo userRepo;

    /**
     * {@inheritDoc}
     */
    @Transactional
    @CacheEvict(value = CacheConstants.HABIT_ITEM_STATISTIC_CACHE, allEntries = true)
    @Override
    public HabitStatisticDto saveByHabitIdAndUserId(Long habitId, Long userId, AddHabitStatisticDto dto) {
        if (habitStatisticRepo.findStatByDateAndHabitIdAndUserId(dto.getCreateDate(), habitId, userId).isPresent()) {
            throw new NotSavedException(ErrorMessage.HABIT_STATISTIC_ALREADY_EXISTS);
        }

        if (isProceed(dto)) {
            HabitStatistic habitStatistic = modelMapper.map(dto, HabitStatistic.class);
            HabitAssign habitAssign = habitAssignRepo.findByHabitIdAndUserId(habitId, userId)
                .orElseThrow(
                    () -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_WITH_CURRENT_USER_ID_AND_HABIT_ID
                        + userId + ", " + habitId));
            habitStatistic.setHabitAssign(habitAssign);
            return modelMapper.map(habitStatisticRepo.save(habitStatistic), HabitStatisticDto.class);
        }
        throw new BadRequestException(ErrorMessage.WRONG_DATE);
    }

    private boolean isProceed(AddHabitStatisticDto dto) {
        return isTodayOrYesterday(
            dateService
                .convertToDatasourceTimezone(dto.getCreateDate())
                .toLocalDate());
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
    public UpdateHabitStatisticDto update(Long habitStatisticId, Long userId, UpdateHabitStatisticDto dto) {
        HabitStatistic updatable = habitStatisticRepo.findById(habitStatisticId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_STATISTIC_NOT_FOUND_BY_ID + habitStatisticId));

        if (updatable.getHabitAssign().getUser().getId().equals(userId)) {
            enhanceHabitStatWithDto(dto, updatable);
            return modelMapper.map(habitStatisticRepo.save(updatable),
                UpdateHabitStatisticDto.class);
        } else {
            throw new BadRequestException(ErrorMessage.HABIT_STATISTIC_NOT_BELONGS_TO_USER + habitStatisticId);
        }
    }

    private void enhanceHabitStatWithDto(UpdateHabitStatisticDto dto, HabitStatistic updatable) {
        updatable.setAmountOfItems(dto.getAmountOfItems());
        updatable.setHabitRate(dto.getHabitRate());
    }

    /**
     * {@inheritDoc}
     *
     * @author Yuriy Olkhovskyi
     */
    @Override
    public HabitStatisticDto findById(Long id) {
        return modelMapper.map(habitStatisticRepo
            .findById(id)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_STATISTIC_NOT_FOUND_BY_ID + id)),
            HabitStatisticDto.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<HabitStatisticDto> findAllStatsByHabitAssignId(Long habitAssignId) {
        HabitAssign habitAssign = habitAssignRepo.findById(habitAssignId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.HABIT_ASSIGN_NOT_FOUND_BY_ID + habitAssignId));
        return modelMapper.map(habitStatisticRepo.findAllByHabitAssignId(habitAssign.getId()),
            new TypeToken<List<HabitStatisticDto>>() {
            }.getType());
    }

    /**
     * {@inheritDoc}
     *
     * @author Yuriy Olkovskyi
     */
    @Override
    public GetHabitStatisticDto findAllStatsByHabitId(Long habitId) {
        Habit habit = habitRepo.findById(habitId).orElseThrow(
            () -> new NotFoundException(ErrorMessage.HABIT_NOT_FOUND_BY_ID + habitId));
        Long amountOfUsersAcquired = habitAssignRepo.findAmountOfUsersAcquired(habit.getId());
        List<HabitStatisticDto> habitStatisticDtoList = habitStatisticRepo.findAllByHabitId(habit.getId()).stream()
            .map(o -> modelMapper.map(o, HabitStatisticDto.class))
            .collect(Collectors.toList());
        return GetHabitStatisticDto.builder()
            .amountOfUsersAcquired(amountOfUsersAcquired)
            .habitStatisticDtoList(habitStatisticDtoList)
            .build();
    }

    /**
     * {@inheritDoc}
     */
    @Cacheable(value = CacheConstants.HABIT_ITEM_STATISTIC_CACHE, key = "#language")
    @Override
    public List<HabitItemsAmountStatisticDto> getTodayStatisticsForAllHabitItems(String language) {
        return habitStatisticRepo.getStatisticsForAllHabitItemsByDate(ZonedDateTime.now(), language).stream()
            .map(it -> HabitItemsAmountStatisticDto.builder()
                .habitItem((String) it.get(0))
                .notTakenItems((long) it.get(1))
                .build())
            .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getAmountOfHabitsInProgressByUserId(Long userId) {
        return habitStatisticRepo.getAmountOfHabitsInProgressByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Long getAmountOfAcquiredHabitsByUserId(Long userId) {
        return habitStatisticRepo.getAmountOfAcquiredHabitsByUserId(userId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAllStatsByHabitAssign(HabitAssignVO habitAssignVO) {
        habitStatisticRepo.deleteAll(habitStatisticRepo.findAllByHabitAssignId(habitAssignVO.getId()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> calculateUserInterest() {
        Long totalActiveUsers = userRepo.countActiveUsers();
        List<Long> creators = habitRepo.countActiveHabitCreators();
        List<Long> followers = habitRepo.countActiveHabitFollowers();
        Set<Long> participatingUsers = new HashSet<>(followers);
        participatingUsers.addAll(creators);

        Long nonParticipatingUsers = totalActiveUsers - participatingUsers.size();

        return Map.of("subscribed", (long) followers.size(),
            "creators", (long) creators.size(),
            "nonParticipants", nonParticipatingUsers);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Long> calculateHabitBehaviorStatistic() {
        List<HabitStatusCount> habitStatusCounts = habitAssignRepo.countHabitAssignsByStatus();
        Map<HabitAssignStatus, Long> counts = habitStatusCounts.stream()
            .collect(Collectors.toMap(
                HabitStatusCount::status,
                HabitStatusCount::count));

        Arrays.stream(HabitAssignStatus.values())
            .forEach(status -> counts.putIfAbsent(status, 0L));

        return Map.of(
            "giveUp", counts.getOrDefault(HabitAssignStatus.CANCELLED, 0L)
                + counts.getOrDefault(HabitAssignStatus.EXPIRED, 0L),
            "successfullyComplete", counts.getOrDefault(HabitAssignStatus.ACQUIRED, 0L),
            "stayWithHabit", counts.getOrDefault(HabitAssignStatus.INPROGRESS, 0L));
    }

    @Override
    public Map<String, List<HabitDateCount>> calculateInteractions(String range) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startDate = calculateStartDate(range);

        List<HabitDateCount> creationStats = habitRepo.countCreationsInRange(startDate, now);
        List<Object[]> subscriptionStatsRaw = habitRepo.countSubscriptionsInRangeRaw(startDate, now);
        List<HabitDateCount> subscriptionStats = mapToHabitDateCount(subscriptionStatsRaw);

        return Map.of(
            "creations", creationStats,
            "subscriptions", subscriptionStats);
    }

    /**
     * Calculate the start date based on the range.
     */
    private LocalDateTime calculateStartDate(String range) {
        return switch (range.toLowerCase()) {
            case "weekly" -> LocalDateTime.now().minusWeeks(1);
            case "monthly" -> LocalDateTime.now().minusMonths(1);
            case "yearly" -> LocalDateTime.now().minusYears(1);
            default -> LocalDateTime.now().minusMonths(1);
        };
    }

    private List<HabitDateCount> mapToHabitDateCount(List<Object[]> results) {
        return results.stream()
            .map(result -> new HabitDateCount((java.sql.Date) result[0], (Long) result[1]))
            .collect(Collectors.toList());
    }
}