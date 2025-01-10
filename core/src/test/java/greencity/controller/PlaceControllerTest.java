package greencity.controller;

import greencity.ModelUtils;
import greencity.converters.UserArgumentResolver;
import greencity.dto.filter.FilterPlacesApiDto;
import greencity.dto.place.PlaceAddDto;
import greencity.dto.place.PlaceUpdateDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.place.AddPlaceDto;
import greencity.dto.place.BulkUpdatePlaceStatusDto;
import greencity.dto.place.PlaceWithUserDto;
import greencity.dto.place.UpdatePlaceStatusWithUserEmailDto;
import greencity.enums.PlaceStatus;
import greencity.service.UserService;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import greencity.client.RestClient;
import greencity.dto.breaktime.BreakTimeDto;
import greencity.dto.category.CategoryDto;
import greencity.dto.discount.DiscountValueDto;
import greencity.dto.favoriteplace.FavoritePlaceDto;
import greencity.dto.filter.FilterPlaceDto;
import greencity.dto.location.LocationAddressAndGeoDto;
import greencity.dto.location.LocationAddressAndGeoForUpdateDto;
import greencity.dto.openhours.OpeningHoursDto;
import greencity.dto.photo.PhotoAddDto;
import greencity.dto.specification.SpecificationNameDto;
import greencity.dto.user.UserVO;
import greencity.enums.UserStatus;
import greencity.service.FavoritePlaceService;
import greencity.service.PlaceService;
import static greencity.ModelUtils.getFilterPlaceDto;
import static greencity.ModelUtils.getUserVO;
import static greencity.ModelUtils.getFilterPlacesApiDto;
import static greencity.ModelUtils.getPlaceByBoundsDto;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static greencity.ModelUtils.getPrincipal;
import static greencity.enums.PlaceStatus.APPROVED;
import static greencity.enums.PlaceStatus.PROPOSED;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PlaceControllerTest {
    private static final String placeLink = "/place";

    private MockMvc mockMvc;

    @InjectMocks
    private PlaceController placeController;

    @Mock
    private PlaceService placeService;

    @Mock
    private UserService userService;

    @Mock
    private FavoritePlaceService favoritePlaceService;

    @Mock
    private RestClient restClient;

    @Mock
    private ModelMapper modelMapper;

    private final Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(placeController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .build();
    }

    @Test
    void proposePlace() throws Exception {
        CategoryDto categoryDto = CategoryDto.builder().name("test").build();
        String json = """
            {
              "category": {
                "name": "test"
              },
              "discountValues": [
                {
                  "specification": {
                    "name": "test"
                  },
                  "value": 1
                }
              ],
              "id": 1,
              "location": {
                "address": "test",
                "lat": 1,
                "lng": 1
              },
              "name": "string",
              "openingHoursList": [
                {
                  "breakTime": {
                    "endTime": "14:00",
                    "startTime": "13:00"
                  },
                  "closeTime": "20:00",
                  "openTime": "08:00",
                  "weekDay": "MONDAY"
                }
              ],
              "photos": [
                {
                  "name": "test"
                }
              ],
              "status": "PROPOSED"
            }
            """;

        LocationAddressAndGeoDto locationAddressAndGeoDto = LocationAddressAndGeoDto.builder()
            .address("Lviv")
            .lat(1.0)
            .lng(1.0)
            .build();

        Set<DiscountValueDto> discountValuesDtos = new HashSet<>();
        DiscountValueDto discountValueDto = new DiscountValueDto();
        SpecificationNameDto specificationNameDto = new SpecificationNameDto();
        specificationNameDto.setName("test");
        discountValueDto.setSpecification(specificationNameDto);
        discountValuesDtos.add(discountValueDto);

        BreakTimeDto breakTimeDto = BreakTimeDto.builder()
            .endTime(LocalTime.of(14, 0))
            .startTime(LocalTime.of(13, 0))
            .build();
        Set<OpeningHoursDto> openingHoursDtos = new HashSet<>();
        OpeningHoursDto openingHoursDto = OpeningHoursDto.builder()
            .breakTime(breakTimeDto)
            .closeTime(LocalTime.of(20, 0))
            .openTime(LocalTime.of(8, 0))
            .weekDay(DayOfWeek.MONDAY)
            .build();
        openingHoursDtos.add(openingHoursDto);

        List<PhotoAddDto> photoAddDtoList = new ArrayList<>();
        PhotoAddDto photoAddDto = new PhotoAddDto();
        photoAddDto.setName("test");
        photoAddDtoList.add(photoAddDto);

        PlaceAddDto placeAddDto = PlaceAddDto.builder()
            .category(categoryDto)
            .discountValues(discountValuesDtos)
            .location(locationAddressAndGeoDto)
            .name("test")
            .openingHoursList(openingHoursDtos)
            .photos(photoAddDtoList)
            .build();

        UserVO user = UserVO.builder()
            .name("Orest")
            .userStatus(UserStatus.ACTIVATED)
            .build();

        PlaceVO place = PlaceVO.builder()
            .id(1L)
            .status(APPROVED)
            .build();

        when(restClient.findByEmail(principal.getName())).thenReturn(user);
        when(placeService.save(placeAddDto, principal.getName())).thenReturn(place);

        when(modelMapper.map(placeService.save(placeAddDto, principal.getName()), PlaceWithUserDto.class))
            .thenReturn(new PlaceWithUserDto());

        mockMvc.perform(post(placeLink + "/propose")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON)
            .principal(principal))
            .andExpect(status().isCreated());

        verify(placeService, times(1))
            .save(placeAddDto, principal.getName());

    }

    @Test
    void updatePlace() throws Exception {
        LocationAddressAndGeoForUpdateDto locationAddressAndGeoDto = LocationAddressAndGeoForUpdateDto.builder()
            .address("Lviv")
            .lat(1.0)
            .lng(1.0)
            .build();

        String json = """
            {
              "category": {
                "name": "test"
              },
              "discountValues": [
                {
                  "specification": {
                    "name": "test"
                  },
                  "value": 1
                }
              ],
              "id": 1,
              "location": {
                "address": "Lviv",
                "lat": 1.0,
                "lng": 1.0
              },
              "name": "test",
              "openingHoursList": [
                {
                  "breakTime": {
                    "endTime": "14:00",
                    "startTime": "13:00"
                  },
                  "closeTime": "20:00",
                  "openTime": "08:00",
                  "weekDay": "MONDAY"
                }
              ]
            }
            """;

        CategoryDto categoryDto = CategoryDto.builder().name("test").build();

        Set<DiscountValueDto> discountValuesDtos = new HashSet<>();
        DiscountValueDto discountValueDto = new DiscountValueDto();
        SpecificationNameDto specificationNameDto = new SpecificationNameDto();
        specificationNameDto.setName("test");
        discountValueDto.setSpecification(specificationNameDto);
        discountValuesDtos.add(discountValueDto);

        BreakTimeDto breakTimeDto = BreakTimeDto.builder()
            .endTime(LocalTime.of(14, 0))
            .startTime(LocalTime.of(13, 0))
            .build();
        Set<OpeningHoursDto> openingHoursDtos = new HashSet<>();
        OpeningHoursDto openingHoursDto = OpeningHoursDto.builder()
            .breakTime(breakTimeDto)
            .closeTime(LocalTime.of(20, 0))
            .openTime(LocalTime.of(8, 0))
            .weekDay(DayOfWeek.MONDAY)
            .build();
        openingHoursDtos.add(openingHoursDto);

        PlaceUpdateDto placeUpdateDto = PlaceUpdateDto.builder()
            .id(1L)
            .category(categoryDto)
            .discountValues(discountValuesDtos)
            .location(locationAddressAndGeoDto)
            .name("test")
            .openingHoursList(openingHoursDtos)
            .build();

        when(modelMapper.map(placeService.update(any()), PlaceUpdateDto.class)).thenReturn(placeUpdateDto);

        this.mockMvc.perform(put(placeLink + "/update")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(placeService).update(placeUpdateDto);
    }

    @Test
    void getInfo() throws Exception {
        this.mockMvc.perform(get(placeLink + "/info/{id}", 1))
            .andExpect(status().isOk());

        verify(placeService, times(1))
            .getInfoById(1L);
    }

    @Test
    void getFavoritePlaceInfo() throws Exception {
        this.mockMvc.perform(get(placeLink + "/info/favorite/{placeId}", 1))
            .andExpect(status().isOk());

        verify(favoritePlaceService, times(1))
            .getInfoFavoritePlace(1L);
    }

    @Test
    void saveAsFavoritePlace() throws Exception {
        String json = """
            {
              "name": "test",
              "placeId": 1
            }
            """;

        FavoritePlaceDto favoritePlaceDto = FavoritePlaceDto.builder().name("test").placeId(1L).build();
        this.mockMvc.perform(post(placeLink + "/save/favorite/")
            .content(json)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(favoritePlaceService).save(favoritePlaceDto, principal.getName());
    }

    @Test
    void getListPlaceLocationByMapsBounds() throws Exception {

        FilterPlaceDto filterPlaceDto = getFilterPlaceDto();
        String json = """
            {
              "discountDto": {
                "discountMax": 1,
                "discountMin": 1,
                "specification": {
                  "name": "test"
                }
              },
              "distanceFromUserDto": {
                "distance": 1,
                "lat": 1,
                "lng": 1
              },
              "mapBoundsDto": {
                "northEastLat": 1,
                "northEastLng": 1,
                "southWestLat": 1,
                "southWestLng": 1
              },
              "searchReg": "test",
              "status": "PROPOSED",
              "time": "10/10/2010 20:00:00"
            }
            """;

        this.mockMvc.perform(post(placeLink + "/getListPlaceLocationByMapsBounds")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(placeService).findPlacesByMapsBounds(filterPlaceDto);

    }

    @Test
    void getPlacesByStatus() throws Exception {
        int pageNumber = 5;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        this.mockMvc.perform(get(placeLink + "/{status}?page=5", APPROVED))
            .andExpect(status().isOk());

        verify(placeService).getPlacesByStatus(APPROVED, pageable);
    }

    @Test
    void getFilteredPlaces() throws Exception {
        UserVO userVO = getUserVO();
        FilterPlaceDto filterPlaceDto = getFilterPlaceDto();
        String json = """
            {
              "discountDto": {
                "discountMax": 1,
                "discountMin": 1,
                "specification": {
                  "name": "test"
                }
              },
              "distanceFromUserDto": {
                "distance": 1,
                "lat": 1,
                "lng": 1
              },
              "mapBoundsDto": {
                "northEastLat": 1,
                "northEastLng": 1,
                "southWestLat": 1,
                "southWestLng": 1
              },
              "searchReg": "test",
              "status": "PROPOSED",
              "time": "10/10/2010 20:00:00"
            }
            """;

        when(userService.findByEmail(anyString())).thenReturn(userVO);

        this.mockMvc.perform(post(placeLink + "/filter")
            .content(json)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(placeService).getPlacesByFilter(filterPlaceDto, userVO);
    }

    @Test
    void getFilteredPlacesFromApi() throws Exception {
        UserVO userVO = getUserVO();
        FilterPlacesApiDto filterDto = getFilterPlacesApiDto();
        String json = """
            {
              "location": {
                "lat": 0,
                "lng": 0
              },
              "radius": 10000,
              "rankBy": "PROMINENCE",
              "keyword": "test",
              "minPrice": "0",
              "maxPrice": "4",
              "openNow": true
            }
            """;
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        when(placeService.getPlacesByFilter(filterDto, userVO)).thenReturn(getPlaceByBoundsDto());

        this.mockMvc.perform(post(placeLink + "/filter/api")
            .content(json)
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(placeService).getPlacesByFilter(filterDto, userVO);
    }

    @Test
    void filterPlaceBySearchPredicate() throws Exception {
        int pageNumber = 5;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        String json = """
            {
              "discountDto": {
                "discountMax": 1,
                "discountMin": 1,
                "specification": {
                  "name": "test"
                }
              },
              "distanceFromUserDto": {
                "distance": 1,
                "lat": 1,
                "lng": 1
              },
              "mapBoundsDto": {
                "northEastLat": 1,
                "northEastLng": 1,
                "southWestLat": 1,
                "southWestLng": 1
              },
              "searchReg": "test",
              "status": "PROPOSED",
              "time": "10/10/2010 20:00:00"
            }
            """;

        FilterPlaceDto filterPlaceDto = getFilterPlaceDto();

        this.mockMvc.perform(post(placeLink + "/filter/predicate?page=5")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(placeService).filterPlaceBySearchPredicate(filterPlaceDto, pageable);

    }

    @Test
    void getPlaceById() throws Exception {
        this.mockMvc.perform(get(placeLink + "/about/{id}", 1))
            .andExpect(status().isOk());

        verify(placeService, times(1))
            .getInfoForUpdatingById(1L);
    }

    @Test
    void bulkUpdateStatuses() throws Exception {
        List<Long> longList = Arrays.asList(1L, 2L);
        String json = """
            {
              "ids": [
                1,
                2
              ],
              "status": "PROPOSED"
            }
            """;

        BulkUpdatePlaceStatusDto bulkUpdatePlaceStatusDto = new BulkUpdatePlaceStatusDto();
        bulkUpdatePlaceStatusDto.setIds(longList);
        bulkUpdatePlaceStatusDto.setStatus(PROPOSED);

        this.mockMvc.perform(patch(placeLink + "/statuses")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(placeService).updateStatuses(bulkUpdatePlaceStatusDto);
    }

    @Test
    void getStatuses() throws Exception {
        this.mockMvc.perform(get(placeLink + "/statuses"))
            .andExpect(status().isOk());

        verify(placeService).getStatuses();
    }

    @Test
    void delete() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete(placeLink + "/{id}", 1))
            .andExpect(status().isOk());

        verify(placeService, times(1))
            .deleteById(1L);
    }

    @Test
    void bulkDelete() throws Exception {
        List<Long> longList = Arrays.asList(1L, 2L);
        this.mockMvc.perform(MockMvcRequestBuilders.delete(placeLink)
            .param("ids", "1,2"))
            .andExpect(status().isOk());

        verify(placeService).bulkDelete(longList);
    }

    @Test
    void allFilterPlaceCategoriesTest() throws Exception {
        this.mockMvc.perform(get(placeLink + "/v2/filteredPlacesCategories"))
            .andExpect(status().isOk());
    }

    @Test
    @SneakyThrows
    void saveEcoPlaceFromUi() {
        String json = """
            {
              "categoryName": "test",
              "locationName": "вулиця Під Дубом, 7Б, Львів, Львівська область, 79000",
              "openingHoursList": [
                {
                  "closeTime": "20:00",
                  "openTime": "08:00",
                  "weekDay": "MONDAY"
                }
              ],
              "placeName": "Форум Львів"
            }
            """;

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        AddPlaceDto addPlaceDtoRequest = objectMapper.readValue(json, AddPlaceDto.class);
        String jsonValue = objectMapper.writeValueAsString(addPlaceDtoRequest);

        MockMultipartFile jsonFile = new MockMultipartFile("dto", "",
            "application/json", jsonValue.getBytes());
        mockMvc.perform(multipart(placeLink + "/v2/save")
            .file(jsonFile)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
    }

    @Test
    void getAllPlaces() throws Exception {
        int pageNumber = 0;
        int pageSize = 5;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        this.mockMvc.perform(get(placeLink + "/all?page=0&&size=5")
            .principal(principal))
            .andExpect(status().isOk());

        verify(placeService, times(1))
            .findAll(pageable, principal);
    }

    @Test
    void updateStatusSuccessfulTest() throws Exception {
        String json = """
            {
              "placeName": "Test Place",
              "newStatus": "APPROVED",
              "userName": "Test User",
              "email": "test@example.com"
            }
            """;
        UpdatePlaceStatusWithUserEmailDto mockDto = new UpdatePlaceStatusWithUserEmailDto();
        mockDto.setPlaceName("Test Place");
        mockDto.setNewStatus(PlaceStatus.APPROVED);
        mockDto.setUserName("Test User");
        mockDto.setEmail("test@example.com");
        when(placeService.updatePlaceStatus(any(UpdatePlaceStatusWithUserEmailDto.class))).thenReturn(mockDto);
        mockMvc.perform(patch(placeLink + "/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json));
        verify(placeService, times(1)).updatePlaceStatus(any(UpdatePlaceStatusWithUserEmailDto.class));
    }

    @Test
    void updateStatusInvalidInputTest() throws Exception {
        String json = """
            {
              "placeName": "",
              "newStatus": "APPROVED",
              "userName": "Test User",
              "email": "invalid-email"
            }
            """;
        mockMvc.perform(patch(placeLink + "/status")
            .contentType(MediaType.APPLICATION_JSON)
            .content(json))
            .andExpect(status().isBadRequest());
        verify(placeService, times(0)).updatePlaceStatus(any(UpdatePlaceStatusWithUserEmailDto.class));
    }
}
