package greencity.service.impl;

import greencity.converters.DateService;
import greencity.dto.habitstatistic.*;
import greencity.dto.user.HabitLogItemDto;
import greencity.entity.Habit;
import greencity.entity.HabitDictionary;
import greencity.entity.HabitStatistic;
import greencity.entity.enums.HabitRate;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotSavedException;
import greencity.repository.HabitRepo;
import greencity.repository.HabitStatisticRepo;
import greencity.service.HabitService;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class HabitStatisticServiceImplTest {
    @Mock
    HabitService habitService;
    @Mock
    HabitRepo habitRepo;
    @Mock
    ModelMapper modelMapper;
    @Mock
    DateService dateService;
    @Mock
    private HabitStatisticRepo habitStatisticRepo;
    private HabitStatisticServiceImpl habitStatisticService;

    private ZonedDateTime zonedDateTime = ZonedDateTime.now();

    private AddHabitStatisticDto addhs = AddHabitStatisticDto
        .builder().amountOfItems(10).habitRate(HabitRate.GOOD)
        .id(1L).habitId(1L).createdOn(ZonedDateTime.now()).build();

    private Habit habit = new Habit(1L, new HabitDictionary(), null, true,
        zonedDateTime, Collections.emptyList(), null);

    private HabitStatistic habitStatistic = new HabitStatistic(
        1L, HabitRate.GOOD, ZonedDateTime.now(), 10, null);

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
        habitStatisticService = new HabitStatisticServiceImpl(habitStatisticRepo, habitRepo,
            habitService, modelMapper, dateService);
    }

    @Test
    void saveTest() {
        when(dateService.convertToDatasourceTimezone(any())).thenReturn(ZonedDateTime.now());
        when(modelMapper.map(addhs, HabitStatistic.class)).thenReturn(habitStatistic);
        when(habitService.getById(anyLong())).thenReturn(habit);
        when(modelMapper.map(habitStatistic, AddHabitStatisticDto.class)).thenReturn(addhs);
        when(habitStatisticService.save(addhs)).thenReturn(addhs);
        assertEquals(addhs, habitStatisticService.save(addhs));
    }

    @Test
    void saveBeforeDayTest() {
        when(dateService.convertToDatasourceTimezone(any())).thenReturn(ZonedDateTime.now().minusDays(1));
        when(modelMapper.map(addhs, HabitStatistic.class)).thenReturn(habitStatistic);
        when(habitService.getById(anyLong())).thenReturn(habit);
        when(modelMapper.map(habitStatistic, AddHabitStatisticDto.class)).thenReturn(addhs);
        when(habitStatisticService.save(addhs)).thenReturn(addhs);
        assertEquals(addhs, habitStatisticService.save(addhs));
    }

    @Test
    void saveExceptionTest() {
        when(habitStatisticRepo.findHabitStatByDate(addhs.getCreatedOn(),
            addhs.getHabitId())).thenReturn(Optional.of(new HabitStatistic()));
        assertThrows(NotSavedException.class, () ->
            habitStatisticService.save(addhs)
        );
    }

    @Test
    void saveExceptionBadRequestTest() {
        when(habitStatisticRepo.findHabitStatByDate(addhs.getCreatedOn(),
            addhs.getHabitId())).thenReturn(Optional.empty());
        when(dateService.convertToDatasourceTimezone(addhs.getCreatedOn())).thenReturn(ZonedDateTime.now().plusDays(2));
        when(modelMapper.map(addhs, HabitStatistic.class)).thenReturn(new HabitStatistic());
        when(modelMapper.map(new HabitStatistic(), AddHabitStatisticDto.class)).thenReturn(addhs);
        when(habitStatisticRepo.save(new HabitStatistic())).thenReturn(new HabitStatistic());
        assertThrows(BadRequestException.class, () ->
            habitStatisticService.save(addhs)
        );
    }

    @Test
    void saveExceptionBadRequestMinusDayTest() {
        when(habitStatisticRepo.findHabitStatByDate(addhs.getCreatedOn(),
            addhs.getHabitId())).thenReturn(Optional.empty());
        when(dateService.convertToDatasourceTimezone(addhs.getCreatedOn()))
            .thenReturn(ZonedDateTime.now().minusDays(2));
        when(modelMapper.map(addhs, HabitStatistic.class)).thenReturn(new HabitStatistic());
        when(modelMapper.map(new HabitStatistic(), AddHabitStatisticDto.class)).thenReturn(addhs);
        when(habitStatisticRepo.save(new HabitStatistic())).thenReturn(new HabitStatistic());
        assertThrows(BadRequestException.class, () ->
            habitStatisticService.save(addhs)
        );
    }

    @Test
    void updateTest() {
        UpdateHabitStatisticDto updateHabStatDto = new UpdateHabitStatisticDto();
        HabitStatistic habitStatistic = new HabitStatistic();
        when(habitStatisticRepo.findById(anyLong())).thenReturn(Optional.of(habitStatistic));
        when(habitStatisticService.update(anyLong(), updateHabStatDto)).thenReturn(updateHabStatDto);

        assertEquals(updateHabStatDto, habitStatisticService.update(anyLong(), updateHabStatDto));
        Mockito.verify(habitStatisticRepo, times(2)).findById(anyLong());
    }

    @Test
    void findByIdTest() {
        HabitStatistic habitStatistic = new HabitStatistic();
        when(habitStatisticRepo.findById(anyLong())).thenReturn(Optional.of(habitStatistic));
        assertEquals(habitStatistic, habitStatisticService.findById(anyLong()));
    }

    @Test
    void findByIdExceptionTest() {
        when(habitStatisticRepo.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
            habitStatisticService.findById(1L)
        );
    }

    @Test
    void findAllHabitsByUserIdTest() {
        List<Habit> habits = Arrays.asList(new Habit(), new Habit());
        when(habitRepo.findAllByUserId(anyLong())).thenReturn(Optional.of(habits));
        assertEquals(habits, habitStatisticService.findAllHabitsByUserId(anyLong()));
    }

    @Test
    void findAllHabitsByUserIdExceptionTest() {
        when(habitRepo.findAllByUserId(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () ->
            habitStatisticService.findAllHabitsByUserId(1L)
        );
    }

    @Test
    void findAllByHabitIdTest() {
        List<HabitStatistic> habitStatistic = new ArrayList<>();
        when(habitStatisticRepo.findAllByHabitId(anyLong())).thenReturn(habitStatistic);

        List<HabitStatisticDto> habitStatisticDtos =
            habitStatistic
                .stream()
                .map(source -> modelMapper.map(source, HabitStatisticDto.class))
                .collect(Collectors.toList());

        assertEquals(habitStatisticService.findAllByHabitId(anyLong()), habitStatisticDtos);
    }

    @Test
    void getTodayStatisticsForAllHabitItemsTest() {
        when(habitStatisticRepo.getStatisticsForAllHabitItemsByDate(ZonedDateTime.now(), "en"))
            .thenReturn(new ArrayList<>());
        assertEquals(new ArrayList<HabitItemsAmountStatisticDto>(),
            habitStatisticService.getTodayStatisticsForAllHabitItems("en"));
    }

    @Test
    void findAllHabitsByStatusExceptionTest() {
        when(habitRepo.findAllByUserId(anyLong())).thenThrow(new NotFoundException(""));
        assertThrows(NotFoundException.class, () ->
            habitStatisticService.findAllHabitsByStatus(1L, false)
        );
    }

    @Test
    void findAllHabitsByStatusTest() {
        List<Habit> list = Collections.singletonList(habit);
        when(habitRepo.findAllByUserId(1L)).thenReturn(Optional.of(list));
        assertEquals(list, habitStatisticService.findAllHabitsByStatus(1L, true));
    }

    @Test
    void getInfoAboutUserHabitsExceptionTest() {
        when(habitRepo.findAllByUserId(anyLong())).thenReturn(Optional.of(Collections.emptyList()));
        assertThrows(NotFoundException.class, () ->
            habitStatisticService.getInfoAboutUserHabits(1L)
        );
    }

    @Test
    void getInfoAboutUserHabitsTest() {
        CalendarUsefulHabitsDto calendarUsefulHabitsDto = new CalendarUsefulHabitsDto();
        calendarUsefulHabitsDto.setAllItemsPerMonth(Collections.singletonList(
            new HabitLogItemDto(null, 0)));
        calendarUsefulHabitsDto.setCreationDate(zonedDateTime);
        calendarUsefulHabitsDto.setDifferenceUnTakenItemsWithPreviousDay(
            Collections.singletonList(new HabitLogItemDto(null, 0)));
        when(habitRepo.findAllByUserId(anyLong())).thenReturn(Optional.of(Collections.singletonList(habit)));
        when(dateService.getDatasourceZonedDateTime()).thenReturn(ZonedDateTime.now());
        when(habitStatisticRepo.getSumOfAllItemsPerMonth(1L, ZonedDateTime.now()))
            .thenReturn(Optional.of(0));
        assertEquals(calendarUsefulHabitsDto, habitStatisticService.getInfoAboutUserHabits(anyLong()));
    }

    @Test
    void getInfoAboutUserHabitsOneTest() {
        CalendarUsefulHabitsDto calendarUsefulHabitsDto = new CalendarUsefulHabitsDto();
        calendarUsefulHabitsDto.setAllItemsPerMonth(Collections.singletonList(
            new HabitLogItemDto(null, 0)));
        calendarUsefulHabitsDto.setCreationDate(zonedDateTime);
        calendarUsefulHabitsDto.setDifferenceUnTakenItemsWithPreviousDay(
            Collections.singletonList(new HabitLogItemDto(null, 0)));
        when(habitRepo.findAllByUserId(anyLong())).thenReturn(Optional.of(Collections.singletonList(habit)));
        when(dateService.getDatasourceZonedDateTime()).thenReturn(zonedDateTime);
        when(habitStatisticRepo.getSumOfAllItemsPerMonth(1L, zonedDateTime))
            .thenReturn(Optional.of(0));
        assertEquals(calendarUsefulHabitsDto, habitStatisticService.getInfoAboutUserHabits(anyLong()));
    }

    @Test
    void findAllHabitsAndTheirStatisticsExceptionTest() {
        when(habitRepo.findAllByUserId(1L)).thenReturn(Optional.of(Collections.emptyList()));
        assertThrows(NotFoundException.class, () ->
            habitStatisticService.findAllHabitsAndTheirStatistics(1L, true, "en")
        );
    }

    @Test
    void getAmountOfHabitsInProgressByUserId() {
        final Long expected = 1L;
        when(habitStatisticRepo.getAmountOfHabitsInProgressByUserId(anyLong())).thenReturn(expected);
        final Long actual = habitStatisticService.getAmountOfHabitsInProgressByUserId(1L);
        assertEquals(expected, actual);
    }

    @Test
    void getAmountOfAcquiredHabitsByUserId() {
        final Long expected = 1L;
        when(habitStatisticRepo.getAmountOfAcquiredHabitsByUserId(anyLong())).thenReturn(expected);
        final Long actual = habitStatisticService.getAmountOfAcquiredHabitsByUserId(1L);
        assertEquals(expected, actual);
    }
}
