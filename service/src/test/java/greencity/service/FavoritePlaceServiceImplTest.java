package greencity.service;

import greencity.ModelUtils;
import greencity.TestConst;
import greencity.client.RestClient;
import greencity.constant.ErrorMessage;
import greencity.dto.favoriteplace.FavoritePlaceDto;
import greencity.dto.favoriteplace.FavoritePlaceVO;
import greencity.dto.location.LocationDto;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.dto.place.PlaceInfoDto;
import greencity.dto.place.PlaceVO;
import greencity.entity.FavoritePlace;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.WrongIdException;
import greencity.repository.FavoritePlaceRepo;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
class FavoritePlaceServiceImplTest {
    @Mock
    private FavoritePlaceRepo favoritePlaceRepo;
    @Mock
    private RestClient restClient;
    @Mock
    private PlaceService placeService;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private FavoritePlaceServiceImpl favoritePlaceService;

    private final FavoritePlaceDto dto = ModelUtils.getFavoritePlaceDto();
    private final FavoritePlace favoritePlace = ModelUtils.getFavoritePlace();
    private final FavoritePlaceVO favoritePlaceVO = ModelUtils.getFavoritePlaceVO();
    private final String userEmail = favoritePlace.getUser().getEmail();
    private final Long favoritePlaceId = favoritePlace.getId();
    private final PlaceByBoundsDto placeByBoundsDto = ModelUtils.getPlaceByBoundsDtoForFindAllTest();

    @Test
    void saveFavoritePlaceAlreadyExistTest() {
        when(placeService.existsById(any())).thenReturn(true);
        when(favoritePlaceRepo.findByPlaceIdAndUserEmail(anyLong(), anyString())).thenReturn(new FavoritePlace());
        when(modelMapper.map(any(FavoritePlaceDto.class), eq(FavoritePlace.class))).thenReturn(favoritePlace);

        Exception exception = assertThrows(
            WrongIdException.class,
            () -> favoritePlaceService.save(dto, userEmail));

        String expectedMessage =
            "Favorite place already exist for this placeId: " + dto.getPlaceId() + " and user with email: " +
                userEmail;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void saveBadUserEmailTest() {
        when(modelMapper.map(any(FavoritePlaceDto.class), eq(FavoritePlace.class))).thenReturn(favoritePlace);

        Exception exception = assertThrows(
            WrongIdException.class,
            () -> favoritePlaceService.save(dto, userEmail));

        String expectedMessage = ErrorMessage.PLACE_NOT_FOUND_BY_ID;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void saveBadPlaceIdTest() {
        dto.setPlaceId(1L);
        when(modelMapper.map(any(FavoritePlaceDto.class), eq(FavoritePlace.class))).thenReturn(favoritePlace);
        when(placeService.existsById(any())).thenReturn(false);

        Exception exception = assertThrows(
            WrongIdException.class, () -> favoritePlaceService.save(dto, userEmail));
        String expectedMessage = ErrorMessage.PLACE_NOT_FOUND_BY_ID;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void saveTest() {
        when(modelMapper.map(any(FavoritePlace.class), eq(FavoritePlaceDto.class))).thenReturn(dto);
        when(modelMapper.map(any(FavoritePlaceDto.class), eq(FavoritePlace.class))).thenReturn(favoritePlace);
        when(placeService.existsById(any())).thenReturn(true);
        when(favoritePlaceRepo.findByPlaceIdAndUserEmail(anyLong(), anyString())).thenReturn(null);
        when(favoritePlaceRepo.save(any())).thenAnswer(invocationOnMock -> invocationOnMock.getArgument(0));

        FavoritePlaceDto actual = favoritePlaceService.save(dto, userEmail);

        verify(restClient, times(1)).findIdByEmail(userEmail);
        verify(placeService, times(1)).existsById(any());
        verify(favoritePlaceRepo, times(1)).findByPlaceIdAndUserEmail(anyLong(), anyString());
        verify(favoritePlaceRepo, times(1)).save(any(FavoritePlace.class));
        verify(modelMapper, times(1)).map(any(FavoritePlaceDto.class), eq(FavoritePlace.class));
        verify(modelMapper, times(1)).map(any(FavoritePlace.class), eq(FavoritePlaceDto.class));
        verify(restClient, times(1)).findIdByEmail(anyString());

        assertEquals(dto, actual);
    }

    @Test
    void deleteByIdAndUserEmail() {
        when(favoritePlaceRepo.findByPlaceIdAndUserEmail(anyLong(), anyString())).thenReturn(favoritePlace);

        assertEquals(favoritePlaceId, favoritePlaceService.deleteByUserEmailAndPlaceId(favoritePlaceId, userEmail));

        verify(favoritePlaceRepo, times(1)).findByPlaceIdAndUserEmail(anyLong(), anyString());
        verify(favoritePlaceRepo, times(1)).delete(any());
    }

    @Test
    void deleteByIdAndUserEmail_FavoritePlaceNotExist() {
        when(favoritePlaceRepo.findByPlaceIdAndUserEmail(anyLong(), anyString())).thenReturn(null);

        Exception exception = assertThrows(
            NotFoundException.class,
            () -> favoritePlaceService.deleteByUserEmailAndPlaceId(favoritePlaceId, userEmail));
        String expectedMessage = ErrorMessage.FAVORITE_PLACE_NOT_FOUND;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void updateTest() {
        when(favoritePlaceRepo.findByPlaceIdAndUserEmail(any(), any())).thenReturn(favoritePlace);
        when(favoritePlaceRepo.save(any(FavoritePlace.class))).thenReturn(favoritePlace);
        when(modelMapper.map(favoritePlace, FavoritePlaceDto.class)).thenReturn(dto);

        FavoritePlaceDto actual = favoritePlaceService.update(dto, userEmail);

        assertEquals(dto, actual);
    }

    @Test
    void updateFavoritePlaceNotExistTest() {
        when(favoritePlaceRepo.findByPlaceIdAndUserEmail(any(), any())).thenReturn(null);

        Exception exception = assertThrows(
            NotFoundException.class,
            () -> favoritePlaceService.update(dto, userEmail));

        String expectedMessage = ErrorMessage.FAVORITE_PLACE_NOT_FOUND;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void findAllTest() {
        List<FavoritePlace> favoritePlaces = new ArrayList<>();
        List<PlaceByBoundsDto> placeByBoundsDtos = new ArrayList<>();
        for (long i = 0; i < 5; i++) {
            favoritePlaces.add(favoritePlace);
            placeByBoundsDtos.add(placeByBoundsDto);
        }
        when(favoritePlaceRepo.findAllByUserEmail(anyString())).thenReturn(favoritePlaces);
        when(modelMapper.map(any(FavoritePlace.class), eq(PlaceByBoundsDto.class))).thenReturn(placeByBoundsDto);
        assertEquals(placeByBoundsDtos, favoritePlaceService.findAllByUserEmail(TestConst.EMAIL));
    }

    @Test
    void findAllWhenNotRecords() {
        when(favoritePlaceRepo.findAllByUserEmail(anyString())).thenReturn(Collections.emptyList());

        boolean isNoRecords = favoritePlaceService.findAllByUserEmail(TestConst.EMAIL).isEmpty();

        assertTrue(isNoRecords);
    }

    @Test
    void getFavoritePlaceInfo() {
        PlaceInfoDto placeInfoDto = new PlaceInfoDto();
        placeInfoDto.setRate(2.0);
        placeInfoDto.setName("name");

        when(favoritePlaceRepo.findByPlaceId(anyLong())).thenReturn(favoritePlace);
        when(modelMapper.map(favoritePlace, FavoritePlaceVO.class)).thenReturn(favoritePlaceVO);
        when(modelMapper.map(any(PlaceVO.class), eq(PlaceInfoDto.class))).thenReturn(placeInfoDto);
        when(placeService.findById(anyLong())).thenReturn(ModelUtils.getPlaceVO());
        when(placeService.averageRate(anyLong())).thenReturn(2.0);

        assertEquals(placeInfoDto, favoritePlaceService.getInfoFavoritePlace(favoritePlaceId));
    }

    @Test
    void getFavoritePlaceInfo_FavoritePlaceNotExist() {
        Exception exception = assertThrows(
            WrongIdException.class,
            () -> favoritePlaceService.getInfoFavoritePlace(2L));
        String expectedMessage = ErrorMessage.FAVORITE_PLACE_NOT_FOUND;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void getFavoritePlaceInfo_PlaceNotExist() {
        when(favoritePlaceRepo.findByPlaceId(2L)).thenReturn(null);

        Exception exception = assertThrows(WrongIdException.class,
            () -> favoritePlaceService.getInfoFavoritePlace(2L));

        String expectedMessage = ErrorMessage.FAVORITE_PLACE_NOT_FOUND;
        String actualMessage = exception.getMessage();

        assertTrue(actualMessage.contains(expectedMessage));
    }

    @Test
    void getFavoritePlaceWithLocation() {
        LocationDto location = new LocationDto();
        location.setId(favoritePlaceVO.getPlace().getLocation().getId());
        location.setLng(favoritePlaceVO.getPlace().getLocation().getLng());
        location.setLat(favoritePlaceVO.getPlace().getLocation().getLat());
        location.setAddress(favoritePlaceVO.getPlace().getLocation().getAddress());
        PlaceByBoundsDto favoritePlaceByBoundsDto =
            new PlaceByBoundsDto(favoritePlaceVO.getId(), favoritePlaceVO.getName(), location);
        when(favoritePlaceRepo.findByPlaceIdAndUserEmail(2L, "test@gmail.com")).thenReturn(favoritePlace);
        when(modelMapper.map(favoritePlace, PlaceByBoundsDto.class)).thenReturn(favoritePlaceByBoundsDto);

        assertEquals(favoritePlaceByBoundsDto,
            favoritePlaceService.getFavoritePlaceWithLocation(2L, "test@gmail.com"));
    }

    @Test
    void getFavoritePlaceWithLocationNotFoundException() {
        when(favoritePlaceRepo.findByPlaceIdAndUserEmail(2L, "test@gmail.com")).thenReturn(null);
        assertThrows(NotFoundException.class,
            () -> favoritePlaceService.getFavoritePlaceWithLocation(2L, "test@gmail.com"));
    }

}
