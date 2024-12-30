package greencity.service;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.dto.PageableDto;
import greencity.dto.category.CategoryDto;
import greencity.dto.category.CategoryDtoResponse;
import greencity.dto.filter.FilterDistanceDto;
import greencity.dto.filter.FilterPlaceDto;
import greencity.dto.language.LanguageVO;
import greencity.dto.location.LocationAddressAndGeoForUpdateDto;
import greencity.dto.location.LocationVO;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.dto.place.UpdatePlaceStatusWithUserEmailDto;
import greencity.dto.place.AddPlaceDto;
import greencity.dto.place.PlaceResponse;
import greencity.dto.place.FilterPlaceCategory;
import greencity.dto.place.FilterAdminPlaceDto;
import greencity.dto.place.PlaceInfoDto;
import greencity.dto.place.BulkUpdatePlaceStatusDto;
import greencity.dto.place.UpdatePlaceStatusDto;
import greencity.dto.place.AdminPlaceDto;
import greencity.dto.place.PlaceAddDto;
import greencity.dto.place.PlaceUpdateDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.search.SearchPlacesDto;
import greencity.dto.user.UserVO;
import greencity.entity.Category;
import greencity.entity.Language;
import greencity.entity.Location;
import greencity.entity.Photo;
import greencity.entity.Place;
import greencity.entity.User;
import greencity.enums.EmailPreference;
import greencity.enums.EmailPreferencePeriodicity;
import greencity.enums.PlaceStatus;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.PlaceStatusException;
import greencity.exception.exceptions.UserBlockedException;
import greencity.repository.CategoryRepo;
import greencity.repository.FavoritePlaceRepo;
import greencity.repository.PhotoRepo;
import greencity.repository.PlaceRepo;
import greencity.repository.UserRepo;
import greencity.repository.options.PlaceFilter;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Path;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
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
import java.util.stream.Collectors;
import static greencity.ModelUtils.getPlace;
import static greencity.ModelUtils.getSearchPlacesDto;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyLong;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.multipart.MultipartFile;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlaceServiceImplTest {
    private final Category category = Category.builder()
        .id(1L)
        .name("test").build();
    private final CategoryDto categoryDto = CategoryDto.builder()
        .name("test")
        .build();
    private final Language language = Language.builder()
        .id(2L)
        .code("en")
        .build();
    private final LanguageVO languageVO = LanguageVO.builder()
        .id(2L)
        .code("en")
        .build();
    private final User user =
        User.builder()
            .id(1L)
            .email("Nazar.stasyuk@gmail.com")
            .name("Nazar Stasyuk")
            .role(Role.ROLE_USER)
            .lastActivityTime(LocalDateTime.now())
            .dateOfRegistration(LocalDateTime.now())
            .language(language)
            .build();
    private final UserVO userVO =
        UserVO.builder()
            .id(1L)
            .email("Nazar.stasyuk@gmail.com")
            .name("Nazar Stasyuk")
            .role(Role.ROLE_USER)
            .lastActivityTime(LocalDateTime.now())
            .dateOfRegistration(LocalDateTime.now())
            .userStatus(UserStatus.ACTIVATED)
            .languageVO(languageVO)
            .build();
    private final UserVO userVOAdmin =
        UserVO.builder()
            .id(1L)
            .email("Nazar.stasyuk@gmail.com")
            .name("Nazar Stasyuk")
            .role(Role.ROLE_ADMIN)
            .lastActivityTime(LocalDateTime.now())
            .dateOfRegistration(LocalDateTime.now())
            .userStatus(UserStatus.ACTIVATED)
            .languageVO(languageVO)
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
    private final LocationVO locationVO = LocationVO.builder()
        .id(1L)
        .address("test")
        .lat(45.456)
        .lng(46.456)
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
    private ProposePlaceServiceImpl proposePlaceMapper;
    @Mock
    private SpecificationService specificationService;
    @Mock
    private DiscountService discountService;
    @Mock
    private CategoryRepo categoryRepo;
    private final ZoneId zoneId = ZoneId.of("Europe/Kiev");
    private PlaceService placeService;
    @Mock
    private GoogleApiService googleApiService;
    @Mock
    UserRepo userRepo;
    @Mock
    UserService userService;
    @Mock
    private FavoritePlaceRepo favoritePlaceRepo;
    @Mock
    private FileService fileService;
    @Mock
    private UserNotificationService userNotificationService;
    @Mock
    private RestClient restClient;
    @Mock
    private PhotoRepo photoRepo;


    @BeforeEach
    void init() {
        placeService = new PlaceServiceImpl(placeRepo, modelMapper, categoryService, locationService,
            specificationService, openingHoursService, userService, discountService, zoneId,
            proposePlaceMapper, categoryRepo, googleApiService, userRepo, favoritePlaceRepo, fileService,
            userNotificationService, restClient, photoRepo);
    }

    @Test
    void saveTest() {
        Place place = getPlace();
        PlaceVO placeVO = ModelUtils.getPlaceVO();
        PlaceAddDto placeAddDto = ModelUtils.getPlaceAddDto();
        when(userService.findByEmail(anyString())).thenReturn(userVOAdmin);
        when(modelMapper.map(placeAddDto, PlaceVO.class)).thenReturn(placeVO);
        when(modelMapper.map(placeVO, Place.class)).thenReturn(place);
        when(categoryRepo.findByName(anyString())).thenReturn(new Category());
        when(placeRepo.save(any())).thenReturn(place);
        when(modelMapper.map(place, PlaceVO.class)).thenReturn(placeVO);
        when(userService.getUsersIdByEmailPreferenceAndEmailPeriodicity(EmailPreference.PLACES,
            EmailPreferencePeriodicity.IMMEDIATELY)).thenReturn(List.of(userVO));

        PlaceVO saved = placeService.save(placeAddDto, user.getEmail());
        assertEquals(placeVO, saved);

        verify(userService).getUsersIdByEmailPreferenceAndEmailPeriodicity(EmailPreference.PLACES,
            EmailPreferencePeriodicity.IMMEDIATELY);
        verify(userNotificationService).createNewNotificationForPlaceAdded(List.of(userVO), placeVO.getId(),
            placeVO.getCategory().getName(), placeVO.getName());
    }

    @Test
    void updateStatusTest() {
        Place genericEntity = getPlace();
        genericEntity.setCategory(ModelUtils.getCategory());
        when(placeRepo.findById(anyLong())).thenReturn(Optional.of(genericEntity));
        when(userService.getUsersIdByEmailPreferenceAndEmailPeriodicity(EmailPreference.PLACES,
            EmailPreferencePeriodicity.IMMEDIATELY)).thenReturn(List.of(userVO));
        when(placeRepo.save(any())).thenReturn(genericEntity);
        placeService.updateStatus(1L, PlaceStatus.APPROVED);
        assertEquals(PlaceStatus.APPROVED, genericEntity.getStatus());

        verify(userNotificationService).createNewNotificationForPlaceAdded(List.of(userVO), genericEntity.getId(),
            genericEntity.getCategory().getName(), genericEntity.getName());
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
        List<Place> places = Collections.singletonList(getPlace());
        List<PlaceVO> voList = Collections.singletonList(modelMapper.map(places, PlaceVO.class));

        when(modelMapper.map(places.getFirst(), PlaceVO.class)).thenReturn(voList.getFirst());
        when(placeRepo.findAllByUserId(1L)).thenReturn(places);

        List<PlaceVO> actual = placeService.getAllCreatedPlacesByUserId(1L);

        assertEquals(voList, actual);
    }

    @Test
    void updateStatusGivenTheSameStatusThenThrowException() {
        Place genericEntity = getPlace();
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
        Place genericEntity = getPlace();
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
        Place place = getPlace();
        place.setDescription("description");
        place.setEmail("http://www.websitetest.com");
        place.setPhotos(new ArrayList<>());

        PlaceInfoDto gen = modelMapper.map(place, PlaceInfoDto.class);
        gen.setRate(1.5);

        when(placeRepo.findById(anyLong())).thenReturn(Optional.of(place));
        when(placeRepo.getAverageRate(anyLong())).thenReturn(1.5);

        PlaceInfoDto res = placeService.getInfoById(1L);

        assertEquals(gen, res);
        verify(placeRepo).findById(anyLong());
        verify(placeRepo).getAverageRate(anyLong());
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
        List<Place> list = List.of(getPlace());
        List<PlaceVO> expectedList = List.of(ModelUtils.getPlaceVO());
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
        Place place = getPlace();
        Page<Place> pages = new PageImpl<>(Collections.singletonList(place), pageable, 1);
        when(placeRepo.findAll(pageable)).thenReturn(pages);
        List<AdminPlaceDto> placeDtos =
            pages.stream().map(elem -> {
                AdminPlaceDto adminPlaceDto = modelMapper.map(place, AdminPlaceDto.class);
                List<String> photoNames = Optional.ofNullable(place.getPhotos())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(Photo::getName)
                    .collect(Collectors.toList());
                adminPlaceDto.setImages(photoNames);
                return adminPlaceDto;
            }).collect(Collectors.toList());
        PageableDto<AdminPlaceDto> expected =
            new PageableDto<>(placeDtos, pages.getTotalElements(), pageable.getPageNumber(), pages.getTotalPages());
        PageableDto<AdminPlaceDto> actual = placeService.findAll(pageable, null);

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
        PageableDto<AdminPlaceDto> actual = placeService.findAll(pageable, null);

        assertEquals(expected, actual);
        verify(placeRepo).findAll(pageable);
    }

    @Test
    void findAllWithPrincipalTest() {
        Pageable pageable = PageRequest.of(0, 1);
        Principal principal = ModelUtils.getPrincipal();
        Place place = getPlace();
        Page<Place> pages = new PageImpl<>(Collections.singletonList(place), pageable, 1);

        when(placeRepo.findAll(pageable)).thenReturn(pages);
        when(favoritePlaceRepo.findAllFavoritePlaceLocationIdsByUserEmail(principal.getName()))
            .thenReturn(Collections.singletonList(1L));

        PageableDto<AdminPlaceDto> resultPageableDto = placeService.findAll(pageable, principal);
        AdminPlaceDto actual = resultPageableDto.getPage().getFirst();

        AdminPlaceDto expected = modelMapper.map(place, AdminPlaceDto.class);
        List<String> photoNames = Optional.ofNullable(place.getPhotos())
            .orElse(Collections.emptyList())
            .stream()
            .map(Photo::getName)
            .collect(Collectors.toList());
        expected.setImages(photoNames);
        expected.setIsFavorite(true);

        assertEquals(expected, actual);
        assertEquals(expected.getIsFavorite(), actual.getIsFavorite());

        verify(placeRepo).findAll(pageable);
        verify(favoritePlaceRepo).findAllFavoritePlaceLocationIdsByUserEmail(principal.getName());
    }

    @Test
    void updateTest() {
        PlaceUpdateDto placeUpdateDto = new PlaceUpdateDto();
        Place place = getPlace();
        placeUpdateDto.setId(1L);
        placeUpdateDto.setOpeningHoursList(new HashSet<>());
        placeUpdateDto.setDiscountValues(new HashSet<>());
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
        Place place = getPlace();
        when(placeRepo.findById(place.getId())).thenReturn(Optional.of(place));
        when(placeRepo.save(place)).thenReturn(place);

        placeService.deleteById(place.getId());

        assertEquals(PlaceStatus.DELETED, place.getStatus());
    }

    @Test
    void findByIdOptionalTest() {
        Place place = getPlace();
        PlaceVO placeVO = ModelUtils.getPlaceVO();
        when(placeRepo.findById(place.getId())).thenReturn(Optional.of(place));
        Optional<PlaceVO> resultOptional = placeService.findByIdOptional(place.getId());

        assertTrue(resultOptional.isPresent());
        assertEquals(placeVO.getId(), resultOptional.get().getId());
        assertEquals(placeVO.getName(), resultOptional.get().getName());
    }

    @Test
    void getInfoForUpdatingByIdTest() {
        Place place = getPlace();
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
        Place place = getPlace();
        when(placeRepo.findById(place.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> placeService.getInfoForUpdatingById(1L));
        verify(placeRepo).findById(place.getId());
        verify(modelMapper, never()).map(any(), any());
    }

    @Test
    void findPlacesByMapsBoundsTest() {
        Place place = getPlace();
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
        Place place = getPlace();
        List<Place> places = Collections.singletonList(place);
        FilterPlaceDto filterDto = new FilterPlaceDto();
        PlaceByBoundsDto placeByBoundsDto = new PlaceByBoundsDto();
        placeByBoundsDto.setId(place.getId());
        List<PlaceByBoundsDto> placeByBoundsDtos = Collections.singletonList(placeByBoundsDto);
        when(placeRepo.findAll((any(PlaceFilter.class)))).thenReturn(places);
        when(modelMapper.map(place, PlaceByBoundsDto.class)).thenReturn(placeByBoundsDto);

        List<PlaceByBoundsDto> result = placeService.getPlacesByFilter(filterDto, null);

        assertEquals(placeByBoundsDtos, result);
        verify(placeRepo).findAll(any(PlaceFilter.class));
        verify(modelMapper).map(place, PlaceByBoundsDto.class);
    }

    @Test
    void getPlacesByFilterWithDistanceFromUserTest() {
        Place place = getPlace();
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

        List<PlaceByBoundsDto> result = placeService.getPlacesByFilter(filterDto, null);

        assertEquals(placeByBoundsDtos, result);
        verify(placeRepo).findAll(any(PlaceFilter.class));
        verify(modelMapper).map(genericEntity1, PlaceByBoundsDto.class);
    }

    @Test
    void filterPlaceBySearchPredicateTest() {
        Place place = getPlace();
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
        Place place = getPlace();
        String searchQuery = "test";
        Pageable pageable = PageRequest.of(0, 1);
        Page<Place> pages = new PageImpl<>(Collections.singletonList(place), pageable, 1);
        when(placeRepo.searchBy(pageable, searchQuery)).thenReturn(pages);
        List<AdminPlaceDto> placeDtos =
            pages.stream().map(elem -> {
                AdminPlaceDto adminPlaceDto = modelMapper.map(place, AdminPlaceDto.class);
                List<String> photoNames = Optional.ofNullable(place.getPhotos())
                    .orElse(Collections.emptyList())
                    .stream()
                    .map(Photo::getName)
                    .collect(Collectors.toList());
                adminPlaceDto.setImages(photoNames);
                return adminPlaceDto;
            }).collect(Collectors.toList());
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
        Place place = getPlace();

        when(modelMapper.map(dto, PlaceResponse.class)).thenReturn(placeResponse);
        when(userRepo.findByEmail(anyString())).thenReturn(Optional.of(ModelUtils.getUser()));
        when(googleApiService.getResultFromGeoCode(dto.getLocationName())).thenReturn(ModelUtils.getGeocodingResult());
        when(modelMapper.map(placeResponse, Place.class)).thenReturn(place);
        when(modelMapper.map(placeResponse.getLocationAddressAndGeoDto(), Location.class))
            .thenReturn(ModelUtils.getLocation());
        when(placeRepo.save(place)).thenReturn(place);
        when(modelMapper.map(place, PlaceResponse.class)).thenReturn(placeResponse);

        assertEquals(placeResponse, placeService.addPlaceFromUi(dto, "test@mail.com", null));

        verify(modelMapper).map(dto, PlaceResponse.class);
        verify(userRepo).findByEmail("test@mail.com");
        verify(googleApiService).getResultFromGeoCode(dto.getLocationName());
        verify(modelMapper).map(placeResponse, Place.class);
        verify(modelMapper).map(placeResponse.getLocationAddressAndGeoDto(), Location.class);
        verify(placeRepo).save(place);
        verify(modelMapper).map(place, PlaceResponse.class);

        MultipartFile multipartFile = ModelUtils.getMultipartFile();
        when(fileService.upload(multipartFile)).thenReturn("/url1");
        assertEquals(placeResponse,
            placeService.addPlaceFromUi(dto, user.getEmail(),
                new MultipartFile[] {multipartFile}));

        MultipartFile[] multipartFiles = ModelUtils.getMultipartFiles();
        when(fileService.upload(multipartFiles[0])).thenReturn("/url1");
        when(fileService.upload(multipartFiles[1])).thenReturn("/url2");
        assertEquals(placeResponse,
            placeService.addPlaceFromUi(dto, ModelUtils.getUser().getEmail(), multipartFiles));
        verify(fileService, times(3)).upload(any(MultipartFile.class));
    }

    @Test
    void addPlaceFromUiThrowsException() {
        AddPlaceDto dto = ModelUtils.getAddPlaceDto();
        PlaceResponse placeResponse = ModelUtils.getPlaceResponse();
        User user = ModelUtils.getUser();
        user.setUserStatus(UserStatus.BLOCKED);
        String email = user.getEmail();

        when(modelMapper.map(dto, PlaceResponse.class)).thenReturn(placeResponse);
        when(userRepo.findByEmail(email)).thenReturn(Optional.of(user));

        assertThrows(UserBlockedException.class, () -> placeService.addPlaceFromUi(dto, email, null));

        verify(modelMapper).map(dto, PlaceResponse.class);
        verify(userRepo).findByEmail(user.getEmail());
    }

    @Test
    void getFilteredPlacesForAdminTest() {
        FilterAdminPlaceDto filterDto = new FilterAdminPlaceDto();
        filterDto.setName("test name");
        filterDto.setStatus("APPROVED");

        Pageable pageable = Pageable.ofSize(10);

        Place place = new Place();
        place.setId(1L);
        place.setName("test name");
        place.setStatus(PlaceStatus.APPROVED);

        List<Place> places = List.of(place);
        Page<Place> page = new PageImpl<>(places, pageable, places.size());

        when(placeRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        PageableDto<AdminPlaceDto> result = placeService.getFilteredPlacesForAdmin(filterDto, pageable);

        assertEquals(1, result.getPage().size());
        assertEquals(places.size(), result.getTotalElements());

        ArgumentCaptor<Specification<Place>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(placeRepo).findAll(specCaptor.capture(), eq(pageable));

        Specification<Place> capturedSpec = specCaptor.getValue();
        assertNotNull(capturedSpec);
    }

    @Test
    void getFilteredPlacesForAdminWithAllFiltersTest() {
        FilterAdminPlaceDto filterDto = new FilterAdminPlaceDto();
        filterDto.setId("1");
        filterDto.setName("test name");
        filterDto.setStatus("APPROVED");
        filterDto.setAuthor("author name");
        filterDto.setAddress("test address");
        Pageable pageable = Pageable.ofSize(10);
        Place place = new Place();
        place.setId(1L);
        place.setName("test name");
        place.setStatus(PlaceStatus.APPROVED);
        List<Place> places = List.of(place);
        Page<Place> page = new PageImpl<>(places, pageable, places.size());

        when(placeRepo.findAll(any(Specification.class), eq(pageable))).thenReturn(page);

        CriteriaBuilder cb = mock(CriteriaBuilder.class);
        CriteriaQuery<?> query = mock(CriteriaQuery.class);
        Root<Place> root = mock(Root.class);
        Join<Object, Object> authorJoin = mock(Join.class);
        Join<Object, Object> locationJoin = mock(Join.class);

        when(root.join("author")).thenReturn(authorJoin);
        when(root.join("location")).thenReturn(locationJoin);

        when(authorJoin.get("name")).thenReturn(mock(Path.class));
        when(locationJoin.get("address")).thenReturn(mock(Path.class));
        when(root.get("id")).thenReturn(mock(Path.class));
        when(root.get("name")).thenReturn(mock(Path.class));
        when(root.get("status")).thenReturn(mock(Path.class));

        Predicate idPredicate = mock(Predicate.class);
        Predicate namePredicate = mock(Predicate.class);
        Predicate statusPredicate = mock(Predicate.class);
        Predicate authorPredicate = mock(Predicate.class);
        Predicate addressPredicate = mock(Predicate.class);
        Predicate combinedPredicate = mock(Predicate.class);

        when(cb.equal(root.get("id"), "1")).thenReturn(idPredicate);
        when(cb.like(root.get("name"), "%test name%")).thenReturn(namePredicate);
        when(cb.equal(root.get("status"), PlaceStatus.APPROVED)).thenReturn(statusPredicate);
        when(cb.like(authorJoin.get("name"), "%author name%")).thenReturn(authorPredicate);
        when(cb.like(locationJoin.get("address"), "%test address%")).thenReturn(addressPredicate);
        when(cb.and(idPredicate, namePredicate, statusPredicate, authorPredicate, addressPredicate))
            .thenReturn(combinedPredicate);

        PageableDto<AdminPlaceDto> result = placeService.getFilteredPlacesForAdmin(filterDto, pageable);
        assertEquals(1, result.getPage().size());
        assertEquals(places.size(), result.getTotalElements());

        ArgumentCaptor<Specification<Place>> specCaptor = ArgumentCaptor.forClass(Specification.class);
        verify(placeRepo).findAll(specCaptor.capture(), eq(pageable));

        Specification<Place> capturedSpec = specCaptor.getValue();

        Predicate predicate = capturedSpec.toPredicate(root, query, cb);

        assertNotNull(predicate);
        verify(cb).equal(root.get("id"), "1");
        verify(cb).like(root.get("name"), "%test name%");
        verify(cb).equal(root.get("status"), PlaceStatus.APPROVED);
        verify(cb).like(authorJoin.get("name"), "%author name%");
        verify(cb).like(locationJoin.get("address"), "%test address%");
    }

    @Test
    void searchTest() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Place place = getPlace();
        place.setCategory(Category.builder().name("Category").build());
        List<Place> places = List.of(place, place);
        PageImpl<Place> page = new PageImpl<>(places, pageRequest, places.size());
        SearchPlacesDto searchPlacesDto = getSearchPlacesDto();

        when(placeRepo.find(pageRequest, "text", null, null)).thenReturn(page);
        when(modelMapper.map(place, SearchPlacesDto.class)).thenReturn(searchPlacesDto);

        PageableDto<SearchPlacesDto> result = placeService.search(pageRequest, "text", null, null);

        assertEquals(List.of(searchPlacesDto, searchPlacesDto), result.getPage());
    }

    @Test
    void updatePlaceStatusWithUserEmailTest() {
        UpdatePlaceStatusWithUserEmailDto dto = new UpdatePlaceStatusWithUserEmailDto();
        dto.setPlaceName("test1");
        dto.setNewStatus(PlaceStatus.APPROVED);
        dto.setEmail("user@example.com");
        Place place = new Place();
        place.setId(1L);
        place.setName("test1");
        place.setStatus(PlaceStatus.PROPOSED);
        when(placeRepo.findByNameIgnoreCase(dto.getPlaceName())).thenReturn(Optional.of(place));
        when(userRepo.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
        when(placeRepo.save(any(Place.class))).thenReturn(place);
        UpdatePlaceStatusWithUserEmailDto result = placeService.updatePlaceStatus(dto);
        assertEquals("test1", result.getPlaceName());
        assertEquals(PlaceStatus.APPROVED, place.getStatus());
        verify(placeRepo).findByNameIgnoreCase(dto.getPlaceName());
        verify(userRepo).findByEmail(dto.getEmail());
        verify(placeRepo).save(place);
    }

    @Test
    void updatePlaceStatusWithPlaceNotFoundTest() {
        UpdatePlaceStatusWithUserEmailDto dto = new UpdatePlaceStatusWithUserEmailDto();
        dto.setPlaceName("nonexistentPlace");
        dto.setNewStatus(PlaceStatus.APPROVED);
        when(placeRepo.findByNameIgnoreCase(dto.getPlaceName())).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> placeService.updatePlaceStatus(dto));
        assertEquals("The place does not exist by this name: nonexistentPlace", exception.getMessage());
        verify(placeRepo).findByNameIgnoreCase(dto.getPlaceName());
        verify(placeRepo, times(0)).save(any(Place.class));
    }

    @Test
    void updatePlaceStatusWithUserNotFoundTest() {
        UpdatePlaceStatusWithUserEmailDto dto = new UpdatePlaceStatusWithUserEmailDto();
        dto.setPlaceName("test1");
        dto.setNewStatus(PlaceStatus.APPROVED);
        dto.setEmail("nonexistent@example.com");
        Place place = new Place();
        place.setId(1L);
        place.setName("test1");
        place.setStatus(PlaceStatus.PROPOSED);
        when(placeRepo.findByNameIgnoreCase(dto.getPlaceName())).thenReturn(Optional.of(place));
        when(userRepo.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> placeService.updatePlaceStatus(dto));
        assertEquals("The user does not exist by this email: nonexistent@example.com", exception.getMessage());
        verify(placeRepo).findByNameIgnoreCase(dto.getPlaceName());
        verify(userRepo).findByEmail(dto.getEmail());
        verify(placeRepo, times(0)).save(any(Place.class));
    }

    @Test
    void updatePlaceStatusWithPlaceAndUserNotFoundTest() {
        UpdatePlaceStatusWithUserEmailDto dto = new UpdatePlaceStatusWithUserEmailDto();
        dto.setPlaceName("nonexistentPlace");
        dto.setNewStatus(PlaceStatus.APPROVED);
        dto.setEmail("nonexistent@example.com");
        when(placeRepo.findByNameIgnoreCase(dto.getPlaceName())).thenReturn(Optional.empty());
        NotFoundException exception = assertThrows(NotFoundException.class, () -> placeService.updatePlaceStatus(dto));
        assertEquals("The place does not exist by this name: nonexistentPlace", exception.getMessage());
        verify(placeRepo).findByNameIgnoreCase(dto.getPlaceName());
        verify(userRepo, times(0)).findByEmail(dto.getEmail());
        verify(placeRepo, times(0)).save(any(Place.class));
    }

    @Test
    void updatePlaceStatusWithoutSendingEmailNotificationTest() {
        UpdatePlaceStatusWithUserEmailDto dto = new UpdatePlaceStatusWithUserEmailDto();
        dto.setPlaceName("test1");
        dto.setNewStatus(PlaceStatus.PROPOSED);
        dto.setEmail("user@example.com");
        Place place = new Place();
        place.setId(1L);
        place.setName("test1");
        place.setStatus(PlaceStatus.PROPOSED);
        when(placeRepo.findByNameIgnoreCase(dto.getPlaceName())).thenReturn(Optional.of(place));
        when(userRepo.findByEmail(dto.getEmail())).thenReturn(Optional.of(user));
        when(placeRepo.save(any(Place.class))).thenReturn(place);
        UpdatePlaceStatusWithUserEmailDto result = placeService.updatePlaceStatus(dto);
        assertEquals("test1", result.getPlaceName());
        assertEquals(PlaceStatus.PROPOSED, place.getStatus());
        verify(placeRepo).findByNameIgnoreCase(dto.getPlaceName());
        verify(userRepo).findByEmail(dto.getEmail());
        verify(placeRepo).save(place);
        verify(restClient, times(0)).sendEmailNotificationChangesPlaceStatus(dto);
    }
}
