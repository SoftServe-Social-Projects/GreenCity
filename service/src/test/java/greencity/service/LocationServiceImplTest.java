package greencity.service;

import greencity.dto.location.LocationVO;
import greencity.entity.Location;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.LocationRepo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LocationServiceImplTest {
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private LocationRepo locationRepo;
    @InjectMocks
    private LocationServiceImpl locationService;

    private final Location location = new Location();
    private final LocationVO locationVO = new LocationVO();

    @Test
    void saveTest() {
        when(modelMapper.map(locationVO, Location.class)).thenReturn(location);
        when(locationRepo.save(location)).thenReturn(location);
        when(modelMapper.map(location, LocationVO.class)).thenReturn(locationVO);

        assertEquals(locationVO, locationService.save(locationVO));
    }

    @Test
    void findByIdTest() {
        when(locationRepo.findById(anyLong())).thenReturn(Optional.of(location));
        when(modelMapper.map(location, LocationVO.class)).thenReturn(locationVO);

        LocationVO foundEntity = locationService.findById(anyLong());

        assertEquals(locationVO, foundEntity);
    }

    @Test
    void findByIdGivenIdNullThenThrowException() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> locationService.findById(null));
    }

    @Test
    void updateTest() {
        when(locationRepo.findById(1L)).thenReturn(Optional.of(location));
        when(modelMapper.map(location, LocationVO.class)).thenReturn(locationVO);
        when(modelMapper.map(locationVO, Location.class)).thenReturn(location);
        when(locationRepo.save(any())).thenReturn(location);

        LocationVO actual = locationService.update(1L, locationVO);

        assertEquals(locationVO, actual);
    }

    @Test
    void updateGivenIdNullThenThrowException() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> locationService.update(null, locationVO));
    }

    @Test
    void deleteByIdTest() {
        when(locationRepo.findById(1L)).thenReturn(Optional.of(location));
        when(modelMapper.map(locationVO, Location.class)).thenReturn(location);

        assertEquals(1L, locationService.deleteById(1L));
    }

    @Test
    void deleteByIdGivenIdNullThenThrowException() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> locationService.deleteById(null));
    }

    @Test
    void findAllTest() {
        List<Location> genericEntities =
            new ArrayList<>(Arrays.asList(new Location(), new Location()));
        List<LocationVO> genericEntitiesVO =
            new ArrayList<>(Arrays.asList(new LocationVO(), new LocationVO()));

        when(locationRepo.findAll()).thenReturn(genericEntities);
        when(modelMapper.map(genericEntities, new TypeToken<List<LocationVO>>() {
        }.getType()))
            .thenReturn(genericEntitiesVO);

        List<LocationVO> foundEntities = locationService.findAll();

        assertEquals(genericEntitiesVO, foundEntities);
    }
}
