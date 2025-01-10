package greencity.service;

import greencity.dto.breaktime.BreakTimeVO;
import greencity.dto.openhours.OpeningHoursVO;
import greencity.dto.place.PlaceVO;
import greencity.entity.OpeningHours;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.OpenHoursRepo;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.*;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OpenHoursServiceImplTest {
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private OpenHoursRepo openHoursRepo;
    @Mock
    private BreakTimeService breakTimeService;
    @InjectMocks
    private OpenHoursServiceImpl openHoursService;
    private final OpeningHours openingHours = OpeningHours.builder()
        .openTime(LocalTime.of(9, 0))
        .closeTime(LocalTime.of(20, 0))
        .build();

    private final OpeningHoursVO openingHoursVO = OpeningHoursVO.builder()
        .openTime(LocalTime.of(9, 0))
        .closeTime(LocalTime.of(20, 0))
        .build();

    @Test
    void saveTestWOBreakTime() {
        when(modelMapper.map(openingHoursVO, OpeningHours.class)).thenReturn(openingHours);
        when(openHoursRepo.save(openingHours)).thenReturn(openingHours);
        when(modelMapper.map(openingHours, OpeningHoursVO.class)).thenReturn(openingHoursVO);
        OpeningHoursVO save = openHoursService.save(openingHoursVO);
        assertEquals(openingHours.getOpenTime(), save.getOpenTime());
    }

    @Test
    void saveTestWithBreakTime() {
        BreakTimeVO localBreakTimeVO = BreakTimeVO.builder()
            .startTime(LocalTime.of(13, 0))
            .endTime(LocalTime.of(14, 0))
            .build();

        openingHoursVO.setBreakTime(localBreakTimeVO);

        when(breakTimeService.save(openingHoursVO.getBreakTime())).thenReturn(localBreakTimeVO);
        when(modelMapper.map(openingHoursVO, OpeningHours.class)).thenReturn(openingHours);
        when(modelMapper.map(openingHours, OpeningHoursVO.class)).thenReturn(openingHoursVO);

        openHoursService.save(openingHoursVO);
        assertEquals(openingHoursVO.getBreakTime(), localBreakTimeVO);
    }

    @Test
    void saveTestWithOpeningHoursBiggerThanEndOpeningHours_ThrowException() {
        OpeningHoursVO localOpeningHours = OpeningHoursVO.builder()
            .openTime(LocalTime.of(20, 0))
            .closeTime(LocalTime.of(9, 0))
            .build();
        Assertions
            .assertThrows(BadRequestException.class,
                () -> openHoursService.save(localOpeningHours));
    }

    @Test
    void saveTestWithStartBreakTimeBiggerThanEndBreakTime_ThrowException() {
        BreakTimeVO localBreakTime = BreakTimeVO.builder()
            .startTime(LocalTime.of(13, 0))
            .endTime(LocalTime.of(21, 0))
            .build();

        OpeningHoursVO localOpeningHours = OpeningHoursVO.builder()
            .openTime(LocalTime.of(9, 0))
            .closeTime(LocalTime.of(20, 0))
            .breakTime(localBreakTime)
            .build();
        Assertions
            .assertThrows(BadRequestException.class,
                () -> openHoursService.save(localOpeningHours));
    }

    @Test
    void findByIdTest() {
        when(openHoursRepo.findById(anyLong())).thenReturn(Optional.of(openingHours));
        when(modelMapper.map(openingHours, OpeningHoursVO.class)).thenReturn(openingHoursVO);
        OpeningHoursVO foundEntity = openHoursService.findById(anyLong());
        assertEquals(openingHoursVO, foundEntity);
    }

    @Test
    void findByIdGivenIdNullThenThrowException() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> openHoursService.findById(null));
    }

    @Test
    void updateTest() {
        openingHoursVO.setId(1L);
        openingHoursVO.setWeekDay(DayOfWeek.FRIDAY);
        when(openHoursRepo.findById(1L)).thenReturn(Optional.of(openingHours));
        when(modelMapper.map(openingHoursVO, OpeningHours.class)).thenReturn(openingHours);
        when(openHoursRepo.save(openingHours)).thenReturn(openingHours);
        when(modelMapper.map(openingHours, OpeningHoursVO.class)).thenReturn(openingHoursVO);
        OpeningHoursVO update = openHoursService.update(1L, openingHoursVO);
        assertEquals(openingHoursVO, update);
    }

    @Test
    void updateGivenIdNullThenThrowException() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> openHoursService.update(null, openingHoursVO));
    }

    @Test
    void deleteByIdTest() {
        when(openHoursRepo.findById(anyLong())).thenReturn(Optional.of(openingHours));

        assertEquals(1L, openHoursService.deleteById(1L));
    }

    @Test
    void deleteByIdGivenIdNullThenThrowException() {
        Assertions.assertThrows(NotFoundException.class, () -> openHoursService.deleteById(null));
    }

    @Test
    void findAllTest() {
        List<OpeningHours> genericEntities =
            new ArrayList<>(Arrays.asList(new OpeningHours(), new OpeningHours()));
        List<OpeningHoursVO> genericEntitiesVO =
            new ArrayList<>(Arrays.asList(new OpeningHoursVO(), new OpeningHoursVO()));

        when(openHoursRepo.findAll()).thenReturn(genericEntities);
        when(modelMapper.map(genericEntities, new TypeToken<List<OpeningHoursVO>>() {
        }.getType())).thenReturn(genericEntitiesVO);
        List<OpeningHoursVO> foundEntities = openHoursService.findAll();

        assertEquals(genericEntitiesVO, foundEntities);
    }

    @Test
    void getOpenHoursByPlaceTest() {
        PlaceVO place = new PlaceVO();
        List<OpeningHours> genericOpeningHours = Arrays.asList(new OpeningHours(), new OpeningHours());
        List<OpeningHoursVO> genericOpeningHoursVO = Arrays.asList(new OpeningHoursVO(), new OpeningHoursVO());

        when(openHoursRepo.findAllByPlace(any())).thenReturn(genericOpeningHours);
        when(modelMapper.map(genericOpeningHours, new TypeToken<List<OpeningHoursVO>>() {
        }.getType())).thenReturn(genericOpeningHoursVO);
        List<OpeningHoursVO> foundOpeningHours = openHoursService.getOpenHoursByPlace(place);

        assertEquals(genericOpeningHours.size(), foundOpeningHours.size());
        Mockito.verify(openHoursRepo).findAllByPlace(any());
    }

    @Test
    void findAllByPlaceIdTest() {
        Set<OpeningHours> genericOpeningHours = new HashSet<>();
        genericOpeningHours.add(new OpeningHours());
        genericOpeningHours.add(new OpeningHours());

        Set<OpeningHoursVO> genericOpeningHoursVO = new HashSet<>();
        genericOpeningHoursVO.add(new OpeningHoursVO());
        genericOpeningHoursVO.add(new OpeningHoursVO());

        when(openHoursRepo.findAllByPlaceId(anyLong())).thenReturn(genericOpeningHours);
        when(modelMapper.map(genericOpeningHours, new TypeToken<Set<OpeningHoursVO>>() {
        }.getType())).thenReturn(genericOpeningHoursVO);

        Set<OpeningHoursVO> foundOpeningHours = openHoursService.findAllByPlaceId(anyLong());

        assertEquals(foundOpeningHours, genericOpeningHoursVO);
        Mockito.verify(openHoursRepo).findAllByPlaceId(any());
    }
}
