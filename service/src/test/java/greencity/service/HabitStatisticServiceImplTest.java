package greencity.service;

import greencity.ModelUtils;
import greencity.converters.DateService;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.habitstatistic.AddHabitStatisticDto;
import greencity.dto.habitstatistic.HabitItemsAmountStatisticDto;
import greencity.dto.habitstatistic.HabitStatisticDto;
import greencity.dto.habitstatistic.UpdateHabitStatisticDto;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitStatistic;
import greencity.enums.HabitRate;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotSavedException;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitRepo;
import greencity.repository.HabitStatisticRepo;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;

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

    private ZonedDateTime zonedDateTime = ZonedDateTime.now();

    private AddHabitStatisticDto addhs = AddHabitStatisticDto
        .builder().amountOfItems(10).habitRate(HabitRate.GOOD)
        .createDate(ZonedDateTime.now()).build();

    private Habit habit = ModelUtils.getHabit();

    private HabitStatisticDto habitStatisticDto =
        HabitStatisticDto.builder().id(1L).amountOfItems(10).habitRate(HabitRate.GOOD)
            .habitAssignId(1L).createDate(zonedDateTime).build();

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
        when(modelMapper.map(habitStatistics, new TypeToken<List<HabitStatisticDto>>() {
        }.getType())).thenReturn(habitStatisticDtos);
        List<HabitStatisticDto> actual = habitStatisticService.findAllStatsByHabitId(1L);
        assertEquals(habitStatisticDtos, actual);
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
        verify(habitStatisticRepo, times(1)).delete(habitAssign.getHabitStatistic().get(0));
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
}