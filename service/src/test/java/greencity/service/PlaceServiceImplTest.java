package greencity.service;

import static greencity.ModelUtils.getFilterPlacesApiDto;
import static greencity.ModelUtils.getPlaceByBoundsDtoFromApi;
import static greencity.ModelUtils.getPlacesSearchResultEn;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.dto.PageableDto;
import greencity.dto.category.CategoryDto;
import greencity.dto.category.CategoryDtoResponse;
import greencity.dto.discount.DiscountValueDto;
import greencity.dto.discount.DiscountValueVO;
import greencity.dto.filter.FilterDistanceDto;
import greencity.dto.filter.FilterPlaceDto;
import greencity.dto.filter.FilterPlacesApiDto;
import greencity.dto.location.LocationAddressAndGeoDto;
import greencity.dto.location.LocationAddressAndGeoForUpdateDto;
import greencity.dto.location.LocationVO;
import greencity.dto.openhours.OpeningHoursDto;
import greencity.dto.openhours.OpeningHoursVO;
import greencity.dto.photo.PhotoAddDto;
import greencity.dto.place.AddPlaceDto;
import greencity.dto.place.AdminPlaceDto;
import greencity.dto.place.BulkUpdatePlaceStatusDto;
import greencity.dto.place.FilterPlaceCategory;
import greencity.dto.place.PlaceAddDto;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.dto.place.PlaceInfoDto;
import greencity.dto.place.PlaceResponse;
import greencity.dto.place.PlaceUpdateDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.place.UpdatePlaceStatusDto;
import greencity.dto.user.UserVO;
import greencity.entity.*;
import greencity.enums.PlaceStatus;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.PlaceAlreadyExistsException;
import greencity.exception.exceptions.PlaceStatusException;
import greencity.exception.exceptions.UserBlockedException;
import greencity.repository.CategoryRepo;
import greencity.repository.FavoritePlaceRepo;
import greencity.repository.PlaceRepo;
import greencity.repository.UserRepo;
import greencity.repository.options.PlaceFilter;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.Spy;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@Slf4j
class PlaceServiceImplTest {
    private final Category category = Category.builder()
        .id(1L)
        .name("test").build();

    private final CategoryDto categoryDto = CategoryDto.builder()
        .name("test")
        .build();

    private final User user =
        User.builder()
            .id(1L)
            .email("Nazar.stasyuk@gmail.com")
            .name("Nazar Stasyuk")
            .role(Role.ROLE_USER)
            .userStatus(UserStatus.ACTIVATED)
            .lastActivityTime(LocalDateTime.now())
            .dateOfRegistration(LocalDateTime.now())
            .build();
    private final UserVO userVO =
        UserVO.builder()
            .id(1L)
            .email("Nazar.stasyuk@gmail.com")
            .name("Nazar Stasyuk")
            .role(Role.ROLE_USER)
            .lastActivityTime(LocalDateTime.now())
            .dateOfRegistration(LocalDateTime.now())
            .build();
    private final Place genericEntity1 = Place.builder()
        .id(1L)
        .name("test1")
        .author(user)
        .status(PlaceStatus.PROPOSED)
        .modifiedDate(ZonedDateTime.now())
        .build();
    private final Place genericEntity2 = Place.builder()
        .id(2L)
        .name("test2")
        .author(user)
        .status(PlaceStatus.PROPOSED)
        .modifiedDate(ZonedDateTime.now())
        .build();
    private final LocationAddressAndGeoDto locationDto = LocationAddressAndGeoDto.builder()
        .address("test")
        .lat(45.456)
        .lng(46.456)
        .build();
    private final Location location = Location.builder()
        .id(1L)
        .address("test")
        .lat(45.456)
        .lng(46.456)
        .build();
    private final LocationVO locationVO = LocationVO.builder()
        .id(1L)
        .address("test")
        .lat(45.456)
        .lng(46.456)
        .build();
    private Set<OpeningHoursDto> openingHoursList = new HashSet<>();
    private Set<OpeningHours> openingHoursListEntity = new HashSet<>();
    private Set<OpeningHoursVO> openingHoursListEntityVO = new HashSet<>();
    private Set<DiscountValue> discountValues = new HashSet<>();
    private Set<DiscountValueDto> discountValuesDto = new HashSet<>();
    private Set<DiscountValueVO> discountValuesVO = new HashSet<>();
    private List<PhotoAddDto> photoDtos = new ArrayList<>();
    private List<Photo> photos = new ArrayList<>();
    private PlaceAddDto placeAddDto = PlaceAddDto.builder()
        .name("Test")
        .category(categoryDto)
        .location(locationDto)
        .openingHoursList(openingHoursList)
        .discountValues(discountValuesDto)
        .photos(photoDtos)
        .build();
    @Mock
    private PlaceRepo placeRepo;
    @Mock
    private CategoryService categoryService;
    @Mock
    private LocationServiceImpl locationService;
    @Mock
    private OpenHoursService openingHoursService;
    @Spy
    private ModelMapper modelMapper;
    @Mock
    private RestClient restClient;
    @Mock
    private ProposePlaceServiceImpl proposePlaceMapper;
    @Mock
    private SpecificationService specificationService;
    @Mock
    private DiscountService discountService;
    @Mock
    private NotificationService notificationService;

    @Mock
    private CategoryRepo categoryRepo;
    private ZoneId zoneId = ZoneId.of("Europe/Kiev");
    private PlaceService placeService;
    @Mock
    private GoogleApiService googleApiService;
    @Mock
    UserRepo userRepo;
    @Mock
    private FavoritePlaceRepo favoritePlaceRepo;

    @BeforeEach
    void init() {
        MockitoAnnotations.initMocks(this);
        placeService = new PlaceServiceImpl(placeRepo, modelMapper, categoryService, locationService,
            specificationService, restClient, openingHoursService, discountService, notificationService, zoneId,
            proposePlaceMapper, categoryRepo, googleApiService, userRepo, favoritePlaceRepo);
    }

    @Test
    void saveTest() {
        Place place = ModelUtils.getPlace();
        PlaceVO placeVO = ModelUtils.getPlaceVO();
        PlaceAddDto placeAddDto = ModelUtils.getPlaceAddDto();
        when(modelMapper.map(placeAddDto, Place.class)).thenReturn(place);
        userVO.setUserStatus(UserStatus.ACTIVATED);
        when(restClient.findByEmail(anyString())).thenReturn(userVO);
        when(categoryRepo.findByName(anyString())).thenReturn(new Category());
        when(placeRepo.save(any())).thenReturn(place);
        when(modelMapper.map(place, PlaceVO.class)).thenReturn(placeVO);

        PlaceVO saved = placeService.save(placeAddDto, user.getEmail());
        assertEquals(placeVO, saved);
    }

    @Test
    void updateStatusTest() {
        Place genericEntity = ModelUtils.getPlace();
        PlaceVO placeVO = ModelUtils.getPlaceVO();
        when(modelMapper.map(genericEntity, PlaceVO.class)).thenReturn(placeVO);
        when(modelMapper.map(placeVO, Place.class)).thenReturn(genericEntity);
        when(placeRepo.findById(anyLong())).thenReturn(Optional.of(genericEntity));
        when(placeRepo.save(any())).thenReturn(genericEntity);
        placeService.updateStatus(1L, PlaceStatus.DECLINED);
        assertEquals(PlaceStatus.DECLINED, genericEntity.getStatus());
    }

    @Test
    void getPlacesByStatusTest() {
        int pageNumber = 0;
        int pageSize = 1;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        Place place = new Place();
        place.setName("Place");

        AdminPlaceDto dto = new AdminPlaceDto();
        dto.setName("Place");

        Page<Place> placesPage = new PageImpl<>(Collections.singletonList(place), pageable, 1);
        List<AdminPlaceDto> listDto = Collections.singletonList(dto);

        PageableDto<AdminPlaceDto> pageableDto = new PageableDto<>(listDto, listDto.size(), 0, 1);
        pageableDto.setPage(listDto);

        when(placeRepo.findAllByStatusOrderByModifiedDateDesc(any(), any())).thenReturn(placesPage);
        when(modelMapper.map(place, AdminPlaceDto.class)).thenReturn(dto);

        assertEquals(pageableDto, placeService.getPlacesByStatus(any(), any()));
        verify(placeRepo, times(1)).findAllByStatusOrderByModifiedDateDesc(any(), any());
    }

    @Test
    void getAllCreatedPlacesByUserId() {
        List<Place> places = Collections.singletonList(ModelUtils.getPlace());
        List<PlaceVO> voList = Collections.singletonList(modelMapper.map(places, PlaceVO.class));

        when(modelMapper.map(places.get(0), PlaceVO.class)).thenReturn(voList.get(0));
        when(placeRepo.findAllByUserId(1L)).thenReturn(places);

        List<PlaceVO> actual = placeService.getAllCreatedPlacesByUserId(1L);

        assertEquals(voList, actual);
    }

    @Test
    void updateStatusGivenTheSameStatusThenThrowException() {
        Place genericEntity = ModelUtils.getPlace();
        PlaceVO placeVO = ModelUtils.getPlaceVO();

        when(modelMapper.map(genericEntity, PlaceVO.class)).thenReturn(placeVO);
        when(modelMapper.map(placeVO, Place.class)).thenReturn(genericEntity);
        when(placeRepo.findById(1L)).thenReturn(Optional.of(genericEntity));

        assertThrows(PlaceStatusException.class, () -> placeService.updateStatus(1L, PlaceStatus.PROPOSED));
    }

    @Test
    void updateStatusGivenPlaceIdNullThenThrowException() {
        assertThrows(NotFoundException.class, () -> placeService.updateStatus(null, PlaceStatus.PROPOSED));
    }

    @Test
    void findByIdTest() {
        Place genericEntity = ModelUtils.getPlace();
        PlaceVO placeVO = ModelUtils.getPlaceVO();
        when(placeRepo.findById(anyLong())).thenReturn(Optional.of(genericEntity));
        when(modelMapper.map(genericEntity, PlaceVO.class)).thenReturn(placeVO);
        PlaceVO result = placeService.findById(anyLong());
        assertEquals(placeVO, result);
    }

    @Test
    void findByIdGivenIdNullThenThrowException() {
        assertThrows(NotFoundException.class, () -> placeService.findById(null));
    }

    @Test
    void findPlaceUpdateDtoTest() {
        Place genericEntity = new Place();
        PlaceUpdateDto placeUpdateDto = new PlaceUpdateDto();
        when(placeRepo.findById(1L)).thenReturn(Optional.of(genericEntity));
        when(modelMapper.map(genericEntity, PlaceUpdateDto.class)).thenReturn(placeUpdateDto);
        PlaceUpdateDto foundEntity = placeService.getInfoForUpdatingById(1L);
        assertEquals(placeUpdateDto, foundEntity);

        verify(placeRepo).findById(1L);
        verify(modelMapper).map(genericEntity, PlaceUpdateDto.class);
    }

    @Test
    void getInfoByIdTest() {
        Place place = ModelUtils.getPlace();
        PlaceInfoDto gen = modelMapper.map(place, PlaceInfoDto.class);
        gen.setRate(1.5);
        when(placeRepo.findById(anyLong())).thenReturn(Optional.of(place));
        when(placeRepo.getAverageRate(anyLong())).thenReturn(1.5);
        PlaceInfoDto res = placeService.getInfoById(anyLong());
        assertEquals(gen, res);
    }

    @Test
    void getInfoByIdNotFoundTest() {
        assertThrows(NotFoundException.class, () -> placeService.getInfoById(null));
    }

    /**
     * @author Zakhar Skaletskyi
     */
    @Test
    void existsById() {
        when(placeRepo.existsById(anyLong())).thenReturn(true);
        assertTrue(placeService.existsById(3L));
        when(placeRepo.existsById(anyLong())).thenReturn(false);
        assertFalse(placeService.existsById(2L));
    }

    /**
     * @author Zakhar Skaletskyi
     */
    @Test
    void averageRate() {
        Double averageRate = 4.0;
        when(placeRepo.getAverageRate(anyLong())).thenReturn(averageRate);
        assertEquals(averageRate, placeService.averageRate(2L));
    }

    @Test
    void updateStatusesTest() {
        BulkUpdatePlaceStatusDto requestDto = new BulkUpdatePlaceStatusDto(
            Arrays.asList(1L, 2L),
            PlaceStatus.DECLINED);
        PlaceVO placeVO1 = ModelUtils.getPlaceVO();
        PlaceVO placeVO2 = ModelUtils.getPlaceVO();
        placeVO2.setId(2L);

        List<UpdatePlaceStatusDto> expected = Arrays.asList(
            new UpdatePlaceStatusDto(1L, PlaceStatus.DECLINED),
            new UpdatePlaceStatusDto(2L, PlaceStatus.DECLINED));

        when(placeRepo.findById(anyLong()))
            .thenReturn(Optional.of(genericEntity1))
            .thenReturn(Optional.of(genericEntity2));
        when(modelMapper.map(genericEntity1, PlaceVO.class)).thenReturn(placeVO1);
        when(modelMapper.map(genericEntity2, PlaceVO.class)).thenReturn(placeVO2);
        when(modelMapper.map(placeVO1, Place.class)).thenReturn(genericEntity1);
        when(modelMapper.map(placeVO2, Place.class)).thenReturn(genericEntity2);
        when(placeRepo.save(genericEntity1)).thenReturn(genericEntity1);
        when(placeRepo.save(genericEntity2)).thenReturn(genericEntity2);
        when(modelMapper.map(genericEntity1, UpdatePlaceStatusDto.class))
            .thenReturn(new UpdatePlaceStatusDto(1L, PlaceStatus.DECLINED));
        when(modelMapper.map(genericEntity2, UpdatePlaceStatusDto.class))
            .thenReturn(new UpdatePlaceStatusDto(2L, PlaceStatus.DECLINED));

        assertEquals(expected, placeService.updateStatuses(requestDto));
    }

    @Test
    void getStatusesTest() {
        List<PlaceStatus> placeStatuses =
            Arrays.asList(PlaceStatus.PROPOSED, PlaceStatus.DECLINED, PlaceStatus.APPROVED, PlaceStatus.DELETED);

        assertEquals(placeStatuses, placeService.getStatuses());
    }

    @Test
    void bulkDelete() {
        List<Long> request = Arrays.asList(1L, 2L);
        PlaceVO placeVO1 = ModelUtils.getPlaceVO();
        PlaceVO placeVO2 = ModelUtils.getPlaceVO();
        placeVO2.setId(2L);
        when(placeRepo.findById(anyLong()))
            .thenReturn(Optional.of(genericEntity1))
            .thenReturn(Optional.of(genericEntity2));
        when(modelMapper.map(genericEntity1, PlaceVO.class)).thenReturn(placeVO1);
        when(modelMapper.map(genericEntity2, PlaceVO.class)).thenReturn(placeVO2);
        when(modelMapper.map(placeVO1, Place.class)).thenReturn(genericEntity1);
        when(modelMapper.map(placeVO2, Place.class)).thenReturn(genericEntity2);
        when(placeRepo.save(genericEntity1)).thenReturn(genericEntity1);
        when(placeRepo.save(genericEntity2)).thenReturn(genericEntity2);
        when(modelMapper.map(genericEntity1, UpdatePlaceStatusDto.class))
            .thenReturn(new UpdatePlaceStatusDto(1L, PlaceStatus.DELETED))
            .thenReturn(new UpdatePlaceStatusDto(2L, PlaceStatus.DELETED));

        assertEquals(2L, placeService.bulkDelete(request));
    }

    @Test
    void findAllTest() {
        List<Place> list = Arrays.asList(ModelUtils.getPlace());
        List<PlaceVO> expectedList = Arrays.asList(ModelUtils.getPlaceVO());
        when(placeRepo.findAll()).thenReturn(list);
        when(modelMapper.map(list, new TypeToken<List<PlaceVO>>() {
        }.getType())).thenReturn(expectedList);
        assertEquals(expectedList, placeService.findAll());

        verify(placeRepo).findAll();
        verify(modelMapper).map(list, new TypeToken<List<PlaceVO>>() {
        }.getType());
    }

    @Test
    void findAllPageableWithoutPrincipalTest() {
        Pageable pageable = PageRequest.of(0, 1);
        Place place = ModelUtils.getPlace();
        Page<Place> pages = new PageImpl<>(Collections.singletonList(place), pageable, 1);
        when(placeRepo.findAll(pageable)).thenReturn(pages);
        List<AdminPlaceDto> placeDtos =
            pages.stream().map(elem -> modelMapper.map(elem, AdminPlaceDto.class)).collect(Collectors.toList());
        PageableDto<AdminPlaceDto> expected =
            new PageableDto<>(placeDtos, pages.getTotalElements(), pageable.getPageNumber(), pages.getTotalPages());
        PageableDto<AdminPlaceDto> actual = placeService.findAll(pageable, "");

        assertEquals(expected, actual);
        verify(placeRepo).findAll(pageable);
    }

    @Test
    void findAllPageableWithEmptyListTest() {
        Pageable pageable = PageRequest.of(0, 1);
        Page<Place> pages = new PageImpl<>(new ArrayList<>(), pageable, 0);
        when(placeRepo.findAll(pageable)).thenReturn(pages);
        List<AdminPlaceDto> placeDtos = new ArrayList<>();
        PageableDto<AdminPlaceDto> expected =
            new PageableDto<>(placeDtos, pages.getTotalElements(), pageable.getPageNumber(), pages.getTotalPages());
        PageableDto<AdminPlaceDto> actual = placeService.findAll(pageable, "");

        assertEquals(expected, actual);
        verify(placeRepo).findAll(pageable);
    }

    @Test
    void findAllWithPrincipalTest() {
        Pageable pageable = PageRequest.of(0, 1);
        Principal principal = ModelUtils.getPrincipal();
        Place place = ModelUtils.getPlace();
        Page<Place> pages = new PageImpl<>(Collections.singletonList(place), pageable, 1);

        when(placeRepo.findAll(pageable)).thenReturn(pages);
        when(favoritePlaceRepo.findAllFavoritePlaceLocationIdsByUserEmail(principal.getName()))
            .thenReturn(Collections.singletonList(1L));

        PageableDto<AdminPlaceDto> resultPageableDto = placeService.findAll(pageable, principal.getName());
        AdminPlaceDto actual = resultPageableDto.getPage().get(0);

        AdminPlaceDto expected = modelMapper.map(place, AdminPlaceDto.class);
        expected.setIsFavorite(true);

        assertEquals(expected, actual);
        assertEquals(expected.getIsFavorite(), actual.getIsFavorite());

        verify(placeRepo).findAll(pageable);
        verify(favoritePlaceRepo).findAllFavoritePlaceLocationIdsByUserEmail(principal.getName());
    }

    @Test
    void updateTest() {
        PlaceUpdateDto placeUpdateDto = new PlaceUpdateDto();
        Place place = ModelUtils.getPlace();
        PlaceVO placeVO = ModelUtils.getPlaceVO();
        placeUpdateDto.setId(1L);
        placeUpdateDto.setOpeningHoursList(openingHoursList);
        placeUpdateDto.setDiscountValues(discountValuesDto);
        placeUpdateDto.setName("new Name");
        placeUpdateDto.setCategory(categoryDto);
        placeUpdateDto.setLocation(new LocationAddressAndGeoForUpdateDto());
        CategoryDtoResponse categoryDtoResponce = CategoryDtoResponse.builder().build();
        when(modelMapper.map(placeUpdateDto.getLocation(), LocationVO.class)).thenReturn(locationVO);
        when(categoryService.findByName(category.getName())).thenReturn(categoryDtoResponce);
        when(modelMapper.map(categoryDtoResponce, Category.class)).thenReturn(category);
        when(placeRepo.findById(placeUpdateDto.getId())).thenReturn(Optional.of(place));

        PlaceVO updatedPlace = placeService.update(placeUpdateDto);

        assertEquals(placeUpdateDto.getName(), updatedPlace.getName());
        assertEquals(placeUpdateDto.getCategory().getName(), updatedPlace.getCategory().getName());
        verify(modelMapper).map(placeUpdateDto.getLocation(), LocationVO.class);
        verify(locationService).update(place.getLocation().getId(), locationVO);
    }

    @Test
    void deleteByIdTest() {
        Place place = ModelUtils.getPlace();
        when(placeRepo.findById(place.getId())).thenReturn(Optional.of(place));
        when(placeRepo.save(place)).thenReturn(place);

        placeService.deleteById(place.getId());

        assertEquals(PlaceStatus.DELETED, place.getStatus());
        verify(notificationService, never()).sendImmediatelyReport(any());
    }

    @Test
    void findByIdOptionalTest() {
        Place place = ModelUtils.getPlace();
        PlaceVO placeVO = ModelUtils.getPlaceVO();
        when(placeRepo.findById(place.getId())).thenReturn(Optional.of(place));
        Optional<PlaceVO> resultOptional = placeService.findByIdOptional(place.getId());

        assertTrue(resultOptional.isPresent());
        assertEquals(placeVO.getId(), resultOptional.get().getId());
        assertEquals(placeVO.getName(), resultOptional.get().getName());
    }

    @Test
    void getInfoForUpdatingByIdTest() {
        Place place = ModelUtils.getPlace();
        PlaceUpdateDto placeUpdateDto = new PlaceUpdateDto();
        placeUpdateDto.setId(place.getId());
        when(placeRepo.findById(place.getId())).thenReturn(Optional.of(place));
        when(modelMapper.map(place, PlaceUpdateDto.class)).thenReturn(placeUpdateDto);

        placeUpdateDto = placeService.getInfoForUpdatingById(place.getId());

        assertEquals(place.getId(), placeUpdateDto.getId());
        verify(placeRepo).findById(place.getId());
        verify(modelMapper).map(place, PlaceUpdateDto.class);
    }

    @Test
    void getInfoForUpdatingThrowingExceptionTest() {
        Place place = ModelUtils.getPlace();
        when(placeRepo.findById(place.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> placeService.getInfoForUpdatingById(1L));
        verify(placeRepo).findById(place.getId());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void findPlacesByMapsBoundsTest() {
        Place place = ModelUtils.getPlace();
        List<Place> places = Collections.singletonList(place);
        PlaceByBoundsDto placeByBoundsDto = new PlaceByBoundsDto();
        FilterPlaceDto filterPlaceDto = new FilterPlaceDto();
        List<PlaceByBoundsDto> dtoList = Collections.singletonList(placeByBoundsDto);
        when(placeRepo.findAll(any(PlaceFilter.class))).thenReturn(places);
        when(modelMapper.map(place, PlaceByBoundsDto.class)).thenReturn(placeByBoundsDto);

        List<PlaceByBoundsDto> result = placeService.findPlacesByMapsBounds(filterPlaceDto);

        assertEquals(dtoList, result);
        verify(placeRepo).findAll(any(PlaceFilter.class));
        verify(modelMapper).map(place, PlaceByBoundsDto.class);
    }

    @Test
    void getPlacesByFilterWithNullDistanceFromUserTest() {
        Place place = ModelUtils.getPlace();
        List<Place> places = Collections.singletonList(place);
        FilterPlaceDto filterDto = new FilterPlaceDto();
        PlaceByBoundsDto placeByBoundsDto = new PlaceByBoundsDto();
        placeByBoundsDto.setId(place.getId());
        List<PlaceByBoundsDto> placeByBoundsDtos = Collections.singletonList(placeByBoundsDto);
        when(placeRepo.findAll((any(PlaceFilter.class)))).thenReturn(places);
        when(modelMapper.map(place, PlaceByBoundsDto.class)).thenReturn(placeByBoundsDto);

        List<PlaceByBoundsDto> result = placeService.getPlacesByFilter(filterDto);

        assertEquals(placeByBoundsDtos, result);
        verify(placeRepo).findAll(any(PlaceFilter.class));
        verify(modelMapper).map(place, PlaceByBoundsDto.class);
    }

    @Test
    void getPlacesByFilterWithDistanceFromUserTest() {
        Place place = ModelUtils.getPlace();
        Location newLocation = new Location();
        newLocation.setLat(-80.0);
        newLocation.setLng(-170.0);
        genericEntity1.setLocation(newLocation);
        List<Place> places = Arrays.asList(place, genericEntity1);
        FilterPlaceDto filterDto = new FilterPlaceDto();
        filterDto.setDistanceFromUserDto(new FilterDistanceDto(-80.0, -170.0, 500.0));
        PlaceByBoundsDto placeByBoundsDto = new PlaceByBoundsDto();
        List<PlaceByBoundsDto> placeByBoundsDtos = Collections.singletonList(placeByBoundsDto);
        when(placeRepo.findAll((any(PlaceFilter.class)))).thenReturn(places);
        when(modelMapper.map(genericEntity1, PlaceByBoundsDto.class)).thenReturn(placeByBoundsDto);

        List<PlaceByBoundsDto> result = placeService.getPlacesByFilter(filterDto);

        assertEquals(placeByBoundsDtos, result);
        verify(placeRepo).findAll(any(PlaceFilter.class));
        verify(modelMapper).map(genericEntity1, PlaceByBoundsDto.class);
    }

    @Test
    void getPlacesByFilterFromApiTest() {
        FilterPlacesApiDto filterDto = getFilterPlacesApiDto();

        when(googleApiService.getResultFromPlacesApi(filterDto, userVO)).thenReturn(getPlacesSearchResultEn());

        var result = placeService.getPlacesByFilter(filterDto, userVO);
        List<PlaceByBoundsDto> updatedResult = result.stream().map(el -> {
            el.setId(1L);
            el.getLocation().setId(1L);
            return el;
        }).collect(Collectors.toList());
        List<PlaceByBoundsDto> expectedResult = getPlaceByBoundsDtoFromApi();

        Assertions.assertArrayEquals(updatedResult.toArray(), expectedResult.toArray());

        verify(googleApiService).getResultFromPlacesApi(filterDto, userVO);
    }

    @Test
    void filterPlaceBySearchPredicateTest() {
        Place place = ModelUtils.getPlace();
        Pageable pageable = PageRequest.of(0, 1);
        Page<Place> pageOfPlaces = new PageImpl<>(Collections.singletonList(place), pageable, 1);
        when(placeRepo.findAll(any(PlaceFilter.class), any(Pageable.class))).thenReturn(pageOfPlaces);
        AdminPlaceDto adminPlaceDto = new AdminPlaceDto();
        PageableDto<AdminPlaceDto> adminPlacePage =
            new PageableDto<>(Collections.singletonList(adminPlaceDto), 1, 0, 1);
        when(modelMapper.map(place, AdminPlaceDto.class)).thenReturn(adminPlaceDto);

        PageableDto<AdminPlaceDto> result = placeService.filterPlaceBySearchPredicate(new FilterPlaceDto(), pageable);

        assertEquals(adminPlacePage, result);
        verify(placeRepo).findAll(any(PlaceFilter.class), any(Pageable.class));
        verify(modelMapper).map(place, AdminPlaceDto.class);
    }

    @Test
    void searchByTest() {
        Place place = ModelUtils.getPlace();
        String searchQuery = "test";
        Pageable pageable = PageRequest.of(0, 1);
        Page<Place> pages = new PageImpl<>(Collections.singletonList(place), pageable, 1);
        when(placeRepo.searchBy(pageable, searchQuery)).thenReturn(pages);
        List<AdminPlaceDto> placeDtos =
            pages.stream().map(p -> modelMapper.map(p, AdminPlaceDto.class)).collect(Collectors.toList());
        PageableDto<AdminPlaceDto> result =
            new PageableDto<>(placeDtos, pages.getTotalElements(), pageable.getPageNumber(), pages.getTotalPages());
        assertEquals(result, placeService.searchBy(pageable, searchQuery));
        verify(placeRepo).searchBy(pageable, searchQuery);
    }

    @Test
    void getAllPlaceCategories() {
        List<Category> categoryList = List.of(ModelUtils.getCategory());
        when(categoryRepo.findAll()).thenReturn(categoryList);
        when(modelMapper.map(categoryList, new TypeToken<List<FilterPlaceCategory>>() {
        }.getType())).thenReturn(List.of(ModelUtils.getFilterPlaceCategory()));

        assertEquals(List.of(ModelUtils.getFilterPlaceCategory()), placeService.getAllPlaceCategories());

        verify(categoryRepo).findAll();
        verify(modelMapper).map(categoryRepo.findAll(), new TypeToken<List<FilterPlaceCategory>>() {
        }.getType());
    }

    @Test
    void addPlaceFromUi() {
        AddPlaceDto dto = ModelUtils.getAddPlaceDto();
        PlaceResponse placeResponse = ModelUtils.getPlaceResponse();
        Place place = ModelUtils.getPlace();

        when(modelMapper.map(dto, PlaceResponse.class)).thenReturn(placeResponse);
        when(userRepo.findByEmail("test@mail.com")).thenReturn(Optional.of(ModelUtils.getUser()));
        when(googleApiService.getResultFromGeoCode(dto.getLocationName())).thenReturn(ModelUtils.getGeocodingResult());
        when(modelMapper.map(placeResponse, Place.class)).thenReturn(place);
        when(modelMapper.map(placeResponse.getLocationAddressAndGeoDto(), Location.class))
            .thenReturn(ModelUtils.getLocation());
        when(placeRepo.save(place)).thenReturn(place);
        when(modelMapper.map(place, PlaceResponse.class)).thenReturn(placeResponse);

        assertEquals(placeResponse, placeService.addPlaceFromUi(dto, "test@mail.com"));

        verify(modelMapper).map(dto, PlaceResponse.class);
        verify(userRepo).findByEmail("test@mail.com");
        verify(googleApiService).getResultFromGeoCode(dto.getLocationName());
        verify(modelMapper).map(placeResponse, Place.class);
        verify(modelMapper).map(placeResponse.getLocationAddressAndGeoDto(), Location.class);
        verify(placeRepo).save(place);
        verify(modelMapper).map(place, PlaceResponse.class);
    }

    @Test
    void addPlaceFromUiThrowsException() {
        AddPlaceDto dto = ModelUtils.getAddPlaceDto();
        PlaceResponse placeResponse = ModelUtils.getPlaceResponse();
        User user = ModelUtils.getUser();
        user.setUserStatus(UserStatus.BLOCKED);

        when(modelMapper.map(dto, PlaceResponse.class)).thenReturn(placeResponse);
        when(userRepo.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(UserBlockedException.class, () -> placeService.addPlaceFromUi(dto, user.getEmail()));

        verify(modelMapper).map(dto, PlaceResponse.class);
        verify(userRepo).findByEmail(user.getEmail());
    }

    @Test
    void addPlaceFromUiSaveAlreadyExistingLocation() {
        AddPlaceDto dto = ModelUtils.getAddPlaceDto();
        PlaceResponse placeResponse = ModelUtils.getPlaceResponse();
        String email = user.getEmail();

        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));
        when(modelMapper.map(dto, PlaceResponse.class)).thenReturn(placeResponse);
        when(googleApiService.getResultFromGeoCode(dto.getLocationName())).thenReturn(ModelUtils.getGeocodingResult());

        double lat = ModelUtils.getGeocodingResult().get(0).geometry.location.lat;
        double lng = ModelUtils.getGeocodingResult().get(0).geometry.location.lng;

        when(locationService.existsByLatAndLng(lat, lng)).thenReturn(true);

        assertThrows(PlaceAlreadyExistsException.class, () -> placeService.addPlaceFromUi(dto, email));

        verify(modelMapper).map(dto, PlaceResponse.class);
        verify(userRepo).findByEmail(user.getEmail());
        verify(googleApiService).getResultFromGeoCode(dto.getLocationName());
        verify(locationService).existsByLatAndLng(lat, lng);

        verify(modelMapper, times(0)).map(placeResponse, Place.class);
    }
}