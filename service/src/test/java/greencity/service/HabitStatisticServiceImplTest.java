package greencity.service;

import greencity.ModelUtils;
import greencity.converters.DateService;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habitstatistic.*;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatistic;
import greencity.enums.HabitAssignStatus;
import greencity.enums.HabitRate;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotSavedException;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitStatisticRepo;
import java.sql.Date;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import greencity.repository.UserRepo;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@Slf4j
@ExtendWith(SpringExtension.class)
class HabitStatisticServiceImplTest {
    @Mock
    HabitAssignRepo habitAssignRepo;
    @Mock
    HabitRepo habitRepo;
    @Mock
    ModelMapper modelMapper;
    @Mock
    DateService dateService;
    @Mock
    private HabitStatisticRepo habitStatisticRepo;
    @InjectMocks
    private HabitStatisticServiceImpl habitStatisticService;
    @Mock
    private UserRepo userRepo;

    private ZonedDateTime zonedDateTime = ZonedDateTime.now();

    private AddHabitStatisticDto addhs = AddHabitStatisticDto
        .builder().amountOfItems(10).habitRate(HabitRate.GOOD)
        .createDate(ZonedDateTime.now()).build();

    private Habit habit = ModelUtils.getHabit();

    private HabitStatisticDto habitStatisticDto =
        HabitStatisticDto.builder().id(1L).amountOfItems(10).habitRate(HabitRate.GOOD)
            .habitAssignId(1L).createDate(zonedDateTime).build();

    private GetHabitStatisticDto getHabitStatisticDto = GetHabitStatisticDto.builder()
        .amountOfUsersAcquired(1L)
        .habitStatisticDtoList(List.of(habitStatisticDto))
        .build();

    private HabitStatistic habitStatistic = ModelUtils.getHabitStatistic();

    private HabitAssign habitAssign = ModelUtils.getHabitAssign();

    private List<HabitStatistic> habitStatistics = Collections.singletonList(habitStatistic);

    private List<HabitStatisticDto> habitStatisticDtos = Collections.singletonList(habitStatisticDto);

    @Test
    void saveByHabitIdAndCorrectUserIdTest() {
        when(habitStatisticRepo.findStatByDateAndHabitIdAndUserId(addhs.getCreateDate(),
                1L, 1L)).thenReturn(Optional.empty());
        when(dateService.convertToDatasourceTimezone(addhs.getCreateDate())).thenReturn(zonedDateTime);
        when(modelMapper.map(addhs, HabitStatistic.class)).thenReturn(habitStatistic);

        when(habitAssignRepo.findByHabitIdAndUserId(1L, 1L)).thenReturn(Optional.of(habitAssign));
        when(habitStatisticRepo.save(habitStatistic)).thenReturn(habitStatistic);
        when(modelMapper.map(habitStatistic, HabitStatisticDto.class)).thenReturn(habitStatisticDto);

        HabitStatisticDto actual = habitStatisticService.saveByHabitIdAndUserId(1L, 1L, addhs);
        assertEquals(habitStatisticDto, actual);
    }

    @Test
    void saveExceptionTest() {
        when(habitStatisticRepo.findStatByDateAndHabitIdAndUserId(addhs.getCreateDate(),
                1L, 1L)).thenReturn(Optional.of(new HabitStatistic()));
        assertThrows(NotSavedException.class, () -> habitStatisticService.saveByHabitIdAndUserId(1L, 1L, addhs));
    }

    @Test
    void saveExceptionWrongHabitAssignTest() {
        when(habitStatisticRepo.findStatByDateAndHabitIdAndUserId(addhs.getCreateDate(),
                1L, 1L)).thenReturn(Optional.empty());
        when(dateService.convertToDatasourceTimezone(addhs.getCreateDate())).thenReturn(zonedDateTime);
        when(modelMapper.map(addhs, HabitStatistic.class)).thenReturn(habitStatistic);
        when(habitAssignRepo.findByHabitIdAndUserId(1L, 1L))
                .thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> habitStatisticService.saveByHabitIdAndUserId(1L, 1L, addhs));
    }

    @Test
    void saveExceptionBadRequestTest() {
        when(habitStatisticRepo.findStatByDateAndHabitIdAndUserId(addhs.getCreateDate(),
                1L, 1L)).thenReturn(Optional.empty());
        when(dateService.convertToDatasourceTimezone(addhs.getCreateDate()))
                .thenReturn(zonedDateTime.plusDays(2));

        assertThrows(BadRequestException.class, () -> habitStatisticService.saveByHabitIdAndUserId(1L, 1L, addhs));
    }

    @Test
    void updateTest() {
        habitStatistic.setHabitAssign(habitAssign);
        UpdateHabitStatisticDto updateHabitStatisticDto = new UpdateHabitStatisticDto();
        when(habitStatisticRepo.findById(1L)).thenReturn(Optional.of(habitStatistic));
        when(habitStatisticRepo.save(habitStatistic)).thenReturn(habitStatistic);
        when(modelMapper.map(habitStatistic, UpdateHabitStatisticDto.class))
            .thenReturn(updateHabitStatisticDto);
        UpdateHabitStatisticDto actual =
            habitStatisticService.update(1L, 1L, updateHabitStatisticDto);
        assertEquals(updateHabitStatisticDto, actual);
    }

    @Test
    void updateStatNotFoundExceptionTest() {
        habitStatistic.setHabitAssign(habitAssign);
        UpdateHabitStatisticDto updateHabitStatisticDto = new UpdateHabitStatisticDto();
        when(habitStatisticRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> habitStatisticService.update(1L, 1L, updateHabitStatisticDto));
    }

    @Test
    void updateNotPresentForUserStatTest() {
        habitStatistic.setHabitAssign(habitAssign);
        habitAssign.getUser().setId(2L);
        UpdateHabitStatisticDto updateHabitStatisticDto = new UpdateHabitStatisticDto();
        when(habitStatisticRepo.findById(1L)).thenReturn(Optional.of(habitStatistic));
        assertThrows(BadRequestException.class,
            () -> habitStatisticService.update(1L, 1L, updateHabitStatisticDto));
    }

    @Test
    void findByIdTest() {
        when(habitStatisticRepo.findById(1L)).thenReturn(Optional.of(habitStatistic));
        when(modelMapper.map(habitStatistic, HabitStatisticDto.class)).thenReturn(habitStatisticDto);
        assertEquals(habitStatisticDto, habitStatisticService.findById(1L));
    }

    @Test
    void findByIdExceptionTest() {
        when(habitStatisticRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> habitStatisticService.findById(1L));
    }

    @Test
    void findAllStatsByHabitAssignIdTest() {
        when(habitAssignRepo.findById(1L)).thenReturn(Optional.of(habitAssign));
        when(habitStatisticRepo.findAllByHabitAssignId(1L)).thenReturn(habitStatistics);
        when(modelMapper.map(habitStatistics, new TypeToken<List<HabitStatisticDto>>() {
        }.getType())).thenReturn(habitStatisticDtos);
        List<HabitStatisticDto> actual = habitStatisticService.findAllStatsByHabitAssignId(1L);
        assertEquals(habitStatisticDtos, actual);
    }

    @Test
    void findAllStatsByWrongHabitAssignIdTest() {
        when(habitAssignRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> habitStatisticService.findAllStatsByHabitAssignId(1L));
    }

    @Test
    void findAllStatsByHabitId() {
        when(habitRepo.findById(1L)).thenReturn(Optional.of(habit));
        when(habitStatisticRepo.findAllByHabitId(1L)).thenReturn(habitStatistics);
        when(modelMapper.map(any(HabitStatistic.class), eq(HabitStatisticDto.class))).thenReturn(habitStatisticDto);
        when(habitAssignRepo.findAmountOfUsersAcquired(1L)).thenReturn(1L);
        GetHabitStatisticDto actual = habitStatisticService.findAllStatsByHabitId(1L);
        assertEquals(getHabitStatisticDto, actual);
    }

    @Test
    void findAllStatsByWrongHabitId() {
        when(habitRepo.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> habitStatisticService.findAllStatsByHabitId(1L));
    }

    @Test
    void getTodayStatisticsForAllHabitItemsTest() {
        when(habitStatisticRepo.getStatisticsForAllHabitItemsByDate(zonedDateTime, "en"))
                .thenReturn(new ArrayList<>());
        assertEquals(new ArrayList<HabitItemsAmountStatisticDto>(),
                habitStatisticService.getTodayStatisticsForAllHabitItems("en"));
    }

    @Test
    void deleteAllStatsByHabitAssignTest() {
        HabitAssignVO habitAssignVO = ModelUtils.getHabitAssignVO();
        when(habitAssignRepo.findById(1L)).thenReturn(Optional.of(habitAssign));
        when(modelMapper.map(habitAssign, HabitAssignVO.class)).thenReturn(habitAssignVO);
        when(habitStatisticRepo.findAllByHabitAssignId(1L)).thenReturn(habitAssign.getHabitStatistic());
        habitStatisticService.deleteAllStatsByHabitAssign(habitAssignVO);
        verify(habitStatisticRepo, times(1)).deleteAll(habitAssign.getHabitStatistic());
    }

    @Test
    void getAmountOfHabitsInProgressByUserIdTest() {
        when(habitStatisticRepo.getAmountOfHabitsInProgressByUserId(1L)).thenReturn(4L);
        assertEquals(4L, habitStatisticRepo.getAmountOfHabitsInProgressByUserId(1L));
    }

    @Test
    void getAmountOfAcquiredHabitsByUserIdTest() {
        when(habitStatisticRepo.getAmountOfAcquiredHabitsByUserId(1L)).thenReturn(4L);
        assertEquals(4L, habitStatisticRepo.getAmountOfAcquiredHabitsByUserId(1L));
    }

    @Test
    void testCalculateUserInterest() {
        when(userRepo.countActiveUsers()).thenReturn(100L);
        when(habitRepo.countActiveHabitCreators()).thenReturn(List.of(1L, 2L, 3L));
        when(habitRepo.countActiveHabitFollowers()).thenReturn(List.of(4L, 5L));

        Map<String, Long> result = habitStatisticService.calculateUserInterest();

        assertNotNull(result);
        assertEquals(2L, result.get("subscribed"));
        assertEquals(3L, result.get("creators"));
        assertEquals(95L, result.get("nonParticipants"));
    }

    @Test
    void calculateHabitBehaviorStatisticTest() {
        List<HabitStatusCount> habitStatusCounts = List.of(
            new HabitStatusCount(HabitAssignStatus.ACQUIRED, 10L),
            new HabitStatusCount(HabitAssignStatus.CANCELLED, 5L),
            new HabitStatusCount(HabitAssignStatus.EXPIRED, 3L),
            new HabitStatusCount(HabitAssignStatus.INPROGRESS, 8L));
        when(habitAssignRepo.countHabitAssignsByStatus()).thenReturn(habitStatusCounts);

        Map<String, Long> result = habitStatisticService.calculateHabitBehaviorStatistic();

        assertNotNull(result);
        assertEquals(8L, result.get("giveUp"));
        assertEquals(10L, result.get("successfullyComplete"));
        assertEquals(8L, result.get("stayWithHabit"));
    }

    @Test
    void calculateInteractionsWeeklyTest() {
        LocalDateTime now = LocalDateTime.now();

        List<HabitDateCount> creationStats = List.of(new HabitDateCount(Date.valueOf(now.toLocalDate()), 5L));
        Object[] row = new Object[] {Date.valueOf(now.toLocalDate()), 3L};
        List<Object[]> subscriptionStatsRaw = new ArrayList<>();
        subscriptionStatsRaw.add(row);
        List<HabitDateCount> subscriptionStats = List.of(new HabitDateCount(Date.valueOf(now.toLocalDate()), 3L));

        when(habitRepo.countCreationsInRange(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(creationStats);
        when(habitRepo.countSubscriptionsInRangeRaw(any(LocalDateTime.class), any(LocalDateTime.class)))
            .thenReturn(subscriptionStatsRaw);

        Map<String, List<HabitDateCount>> result = habitStatisticService.calculateInteractions("weekly");

        assertNotNull(result);
        assertEquals(2, result.size());

        assertTrue(result.containsKey("creations"));
        assertEquals(creationStats, result.get("creations"));

        assertTrue(result.containsKey("subscriptions"));
        assertEquals(subscriptionStats, result.get("subscriptions"));
    }

    @ParameterizedTest
    @ValueSource(strings = {"weekly", "monthly", "yearly", "invalid"})
    void calculateStartDateTest(String range) {
        LocalDateTime startDate = invokeCalculateStartDate(range);
        LocalDateTime expectedStartDate;

        switch (range.toLowerCase()) {
            case "weekly":
                expectedStartDate = LocalDateTime.now().minusWeeks(1);
                break;
            case "monthly":
                expectedStartDate = LocalDateTime.now().minusMonths(1);
                break;
            case "yearly":
                expectedStartDate = LocalDateTime.now().minusYears(1);
                break;
            default:
                expectedStartDate = LocalDateTime.now().minusMonths(1);
                break;
        }

        assertEquals(expectedStartDate.toLocalDate(), startDate.toLocalDate());
    }

    @SneakyThrows
    private LocalDateTime invokeCalculateStartDate(String range) {
        var method = HabitStatisticServiceImpl.class.getDeclaredMethod("calculateStartDate", String.class);
        method.setAccessible(true);
        return (LocalDateTime) method.invoke(habitStatisticService, range);
    }

    @Test
    void mapToHabitDateCountTest() {
        Object[] row1 = {Date.valueOf("2024-12-01"), 5L};
        Object[] row2 = {Date.valueOf("2024-12-02"), 10L};
        List<Object[]> results = List.of(row1, row2);

        List<HabitDateCount> habitDateCounts = invokeMapToHabitDateCount(results);

        assertNotNull(habitDateCounts);
        assertEquals(2, habitDateCounts.size());

        HabitDateCount first = habitDateCounts.getFirst();
        assertEquals(Date.valueOf("2024-12-01").toLocalDate(), first.date());
        assertEquals(5L, first.count());

        HabitDateCount second = habitDateCounts.get(1);
        assertEquals(Date.valueOf("2024-12-02").toLocalDate(), second.date());
        assertEquals(10L, second.count());
    }

    @SneakyThrows
    private List<HabitDateCount> invokeMapToHabitDateCount(List<Object[]> results) {
        var method = HabitStatisticServiceImpl.class.getDeclaredMethod("mapToHabitDateCount", List.class);
        method.setAccessible(true);
        return (List<HabitDateCount>) method.invoke(habitStatisticService, results);
    }
}
