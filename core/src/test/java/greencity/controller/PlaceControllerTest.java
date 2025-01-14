package greencity.controller;

import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import greencity.dto.filter.FilterPlacesApiDto;
import greencity.service.UserService;
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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import greencity.client.RestClient;
import greencity.dto.breaktime.BreakTimeDto;
import greencity.dto.category.CategoryDto;
import greencity.dto.discount.DiscountValueDto;
import greencity.dto.favoriteplace.FavoritePlaceDto;
import greencity.dto.filter.FilterDiscountDto;
import greencity.dto.filter.FilterDistanceDto;
import greencity.dto.filter.FilterPlaceDto;
import greencity.dto.location.LocationAddressAndGeoDto;
import greencity.dto.location.LocationAddressAndGeoForUpdateDto;
import greencity.dto.location.MapBoundsDto;
import greencity.dto.openhours.OpeningHoursDto;
import greencity.dto.photo.PhotoAddDto;
import greencity.dto.place.BulkUpdatePlaceStatusDto;
import greencity.dto.place.PlaceAddDto;
import greencity.dto.place.PlaceUpdateDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.place.PlaceWithUserDto;
import greencity.dto.place.UpdatePlaceStatusDto;
import greencity.dto.specification.SpecificationNameDto;
import greencity.dto.user.UserVO;
import greencity.enums.UserStatus;
import greencity.service.FavoritePlaceService;
import greencity.service.PlaceService;

import static greencity.ModelUtils.getFilterPlacesApiDto;
import static greencity.ModelUtils.getPlaceByBoundsDto;
import static greencity.ModelUtils.getPrincipalForUserVO;
import static greencity.ModelUtils.getUserVO;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.anyObject;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static greencity.ModelUtils.getPrincipal;
import static greencity.enums.PlaceStatus.APPROVED;
import static greencity.enums.PlaceStatus.PROPOSED;

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
    private FavoritePlaceService favoritePlaceService;

    @Mock
    private RestClient restClient;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    UserService userService;

    private final Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(placeController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void proposePlace() throws Exception {
        Principal principal = mock(Principal.class);
        CategoryDto categoryDto = CategoryDto.builder().name("test").build();

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
            .content("{\n" +
                "  \"category\": {\n" +
                "    \"name\": \"test\"\n" +
                "  },\n" +
                "  \"discountValues\": [\n" +
                "    {\n" +
                "      \"specification\": {\n" +
                "        \"name\": \"test\"\n" +
                "      },\n" +
                "      \"value\": 1\n" +
                "    }\n" +
                "  ],\n" +
                "  \"id\": 1,\n" +
                "  \"location\": {\n" +
                "    \"address\": \"test\",\n" +
                "    \"lat\": 1,\n" +
                "    \"lng\": 1\n" +
                "  },\n" +
                "  \"name\": \"string\",\n" +
                "  \"openingHoursList\": [\n" +
                "    {\n" +
                "      \"breakTime\": {\n" +
                "        \"endTime\": \"14:00\",\n" +
                "        \"startTime\": \"13:00\"\n" +
                "      },\n" +
                "      \"closeTime\": \"20:00\",\n" +
                "      \"openTime\": \"08:00\",\n" +
                "      \"weekDay\": \"MONDAY\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"photos\": [\n" +
                "    {\n" +
                "      \"name\": \"test\"\n" +
                "    }\n" +
                "  ],\n" +
                "  \"status\": \"PROPOSED\"\n" +
                "}")
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

        when(modelMapper.map(placeService.update(anyObject()), PlaceUpdateDto.class)).thenReturn(placeUpdateDto);

        this.mockMvc.perform(put(placeLink + "/update")
            .content("{\n" +
                "  \"category\": {\n" +
                "    \"name\": \"test\"\n" +
                "  },\n" +
                "  \"discountValues\": [\n" +
                "    {\n" +
                "      \"specification\": {\n" +
                "        \"name\": \"test\"\n" +
                "      },\n" +
                "      \"value\": 1\n" +
                "    }\n" +
                "  ],\n" +
                "  \"id\": 1,\n" +
                "  \"location\": {\n" +
                "    \"address\": \"Lviv\",\n" +
                "    \"lat\": 1.0,\n" +
                "    \"lng\": 1.0\n" +
                "  },\n" +
                "  \"name\": \"test\",\n" +
                "  \"openingHoursList\": [\n" +
                "    {\n" +
                "      \"breakTime\": {\n" +
                "        \"endTime\": \"14:00\",\n" +
                "        \"startTime\": \"13:00\"\n" +
                "      },\n" +
                "      \"closeTime\": \"20:00\",\n" +
                "      \"openTime\": \"08:00\",\n" +
                "      \"weekDay\": \"MONDAY\"\n" +
                "    }\n" +
                "  ]\n" +
                "}")
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
        Principal principal = mock(Principal.class);
        FavoritePlaceDto favoritePlaceDto = FavoritePlaceDto.builder().name("test").placeId(1L).build();
        this.mockMvc.perform(post(placeLink + "/save/favorite/")
            .content("{\n" +
                "  \"name\": \"test\",\n" +
                "  \"placeId\": 1\n" +
                "}")
            .principal(principal)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(favoritePlaceService).save(favoritePlaceDto, principal.getName());
    }

    @Test
    void getListPlaceLocationByMapsBounds() throws Exception {

        FilterPlaceDto filterPlaceDto = new FilterPlaceDto();
        filterPlaceDto.setDistanceFromUserDto(new FilterDistanceDto(1.0, 1.0, 1.0));
        filterPlaceDto.setMapBoundsDto(new MapBoundsDto(1.0, 1.0, 1.0, 1.0));
        filterPlaceDto.setTime("10/10/2010 20:00:00");
        filterPlaceDto.setStatus(PROPOSED);
        filterPlaceDto.setSearchReg("test");

        SpecificationNameDto specificationNameDto = new SpecificationNameDto();
        specificationNameDto.setName("test");

        FilterDiscountDto filterDistanceDto = new FilterDiscountDto();
        filterDistanceDto.setDiscountMax(1);
        filterDistanceDto.setDiscountMin(1);
        filterDistanceDto.setSpecification(specificationNameDto);

        filterPlaceDto.setDiscountDto(filterDistanceDto);

        this.mockMvc.perform(post(placeLink + "/getListPlaceLocationByMapsBounds")
            .content("{\n" +
                "  \"discountDto\": {\n" +
                "    \"discountMax\": 1,\n" +
                "    \"discountMin\": 1,\n" +
                "    \"specification\": {\n" +
                "      \"name\": \"test\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"distanceFromUserDto\": {\n" +
                "    \"distance\": 1,\n" +
                "    \"lat\": 1,\n" +
                "    \"lng\": 1\n" +
                "  },\n" +
                "  \"mapBoundsDto\": {\n" +
                "    \"northEastLat\": 1,\n" +
                "    \"northEastLng\": 1,\n" +
                "    \"southWestLat\": 1,\n" +
                "    \"southWestLng\": 1\n" +
                "  },\n" +
                "  \"searchReg\": \"test\",\n" +
                "  \"status\": \"PROPOSED\",\n" +
                "  \"time\": \"10/10/2010 20:00:00\"\n" +
                "}")
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

        FilterPlaceDto filterPlaceDto = new FilterPlaceDto();
        filterPlaceDto.setDistanceFromUserDto(new FilterDistanceDto(1.0, 1.0, 1.0));
        filterPlaceDto.setMapBoundsDto(new MapBoundsDto(1.0, 1.0, 1.0, 1.0));
        filterPlaceDto.setTime("10/10/2010 20:00:00");
        filterPlaceDto.setStatus(PROPOSED);
        filterPlaceDto.setSearchReg("test");

        SpecificationNameDto specificationNameDto = new SpecificationNameDto();
        specificationNameDto.setName("test");

        FilterDiscountDto filterDistanceDto = new FilterDiscountDto();
        filterDistanceDto.setDiscountMax(1);
        filterDistanceDto.setDiscountMin(1);
        filterDistanceDto.setSpecification(specificationNameDto);

        filterPlaceDto.setDiscountDto(filterDistanceDto);

        this.mockMvc.perform(post(placeLink + "/filter")
            .content("{\n" +
                "  \"discountDto\": {\n" +
                "    \"discountMax\": 1,\n" +
                "    \"discountMin\": 1,\n" +
                "    \"specification\": {\n" +
                "      \"name\": \"test\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"distanceFromUserDto\": {\n" +
                "    \"distance\": 1,\n" +
                "    \"lat\": 1,\n" +
                "    \"lng\": 1\n" +
                "  },\n" +
                "  \"mapBoundsDto\": {\n" +
                "    \"northEastLat\": 1,\n" +
                "    \"northEastLng\": 1,\n" +
                "    \"southWestLat\": 1,\n" +
                "    \"southWestLng\": 1\n" +
                "  },\n" +
                "  \"searchReg\": \"test\",\n" +
                "  \"status\": \"PROPOSED\",\n" +
                "  \"time\": \"10/10/2010 20:00:00\"\n" +
                "}")

            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(placeService).getPlacesByFilter(filterPlaceDto);
    }

    @Test
    void getFilteredPlacesFromApi() throws Exception {
        UserVO userVO = new UserVO();
        Principal principalForUserVO = getPrincipalForUserVO();
        FilterPlacesApiDto filterDto = getFilterPlacesApiDto();
        String json = "{\n" +
            "  \"location\": {\n" +
            "    \"lat\": 0,\n" +
            "    \"lng\": 0\n" +
            "  },\n" +
            "  \"radius\": 10000,\n" +
            "  \"rankBy\": \"PROMINENCE\",\n" +
            "  \"keyword\": \"test\",\n" +
            "  \"minPrice\": \"0\",\n" +
            "  \"maxPrice\": \"4\",\n" +
            "  \"openNow\": true\n" +
            "}\n";
        when(userService.findByEmail(userVO.getEmail())).thenReturn(userVO);
        when(placeService.getPlacesByFilter(filterDto, userVO)).thenReturn(getPlaceByBoundsDto());

        this.mockMvc.perform(post(placeLink + "/filter/api")
            .content(json)
            .principal(principalForUserVO)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(placeService).getPlacesByFilter(filterDto, userVO);
    }

    @Test
    void updateStatus() throws Exception {
        UpdatePlaceStatusDto updatePlaceStatusDto = new UpdatePlaceStatusDto();
        updatePlaceStatusDto.setId(1L);
        updatePlaceStatusDto.setStatus(PROPOSED);
        this.mockMvc.perform(patch(placeLink + "/status")
            .content("{\n" +
                "  \"id\": 1,\n" +
                "  \"status\": \"PROPOSED\"\n" +
                "}")
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(placeService).updateStatus(updatePlaceStatusDto.getId(), updatePlaceStatusDto.getStatus());

    }

    @Test
    void filterPlaceBySearchPredicate() throws Exception {
        int pageNumber = 5;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        FilterPlaceDto filterPlaceDto = new FilterPlaceDto();
        filterPlaceDto.setDistanceFromUserDto(new FilterDistanceDto(1.0, 1.0, 1.0));
        filterPlaceDto.setMapBoundsDto(new MapBoundsDto(1.0, 1.0, 1.0, 1.0));
        filterPlaceDto.setTime("10/10/2010 20:00:00");
        filterPlaceDto.setStatus(PROPOSED);
        filterPlaceDto.setSearchReg("test");

        SpecificationNameDto specificationNameDto = new SpecificationNameDto();
        specificationNameDto.setName("test");

        FilterDiscountDto filterDistanceDto = new FilterDiscountDto();
        filterDistanceDto.setDiscountMax(1);
        filterDistanceDto.setDiscountMin(1);
        filterDistanceDto.setSpecification(specificationNameDto);

        filterPlaceDto.setDiscountDto(filterDistanceDto);

        this.mockMvc.perform(post(placeLink + "/filter/predicate?page=5")
            .content("{\n" +
                "  \"discountDto\": {\n" +
                "    \"discountMax\": 1,\n" +
                "    \"discountMin\": 1,\n" +
                "    \"specification\": {\n" +
                "      \"name\": \"test\"\n" +
                "    }\n" +
                "  },\n" +
                "  \"distanceFromUserDto\": {\n" +
                "    \"distance\": 1,\n" +
                "    \"lat\": 1,\n" +
                "    \"lng\": 1\n" +
                "  },\n" +
                "  \"mapBoundsDto\": {\n" +
                "    \"northEastLat\": 1,\n" +
                "    \"northEastLng\": 1,\n" +
                "    \"southWestLat\": 1,\n" +
                "    \"southWestLng\": 1\n" +
                "  },\n" +
                "  \"searchReg\": \"test\",\n" +
                "  \"status\": \"PROPOSED\",\n" +
                "  \"time\": \"10/10/2010 20:00:00\"\n" +
                "}")
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

        BulkUpdatePlaceStatusDto bulkUpdatePlaceStatusDto = new BulkUpdatePlaceStatusDto();
        bulkUpdatePlaceStatusDto.setIds(longList);
        bulkUpdatePlaceStatusDto.setStatus(PROPOSED);

        this.mockMvc.perform(patch(placeLink + "/statuses")
            .content("{\n" +
                "  \"ids\": [\n" +
                "    1,\n" +
                "    2\n" +
                "  ],\n" +
                "  \"status\": \"PROPOSED\"\n" +
                "}")
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
    void saveEcoPlaceFromUi() throws Exception {
        String json = "{\n" +
            "  \"categoryName\": \"test\",\n" +
            "  \"locationName\": \"вулиця Під Дубом, 7Б, Львів, Львівська область, 79000\",\n" +
            "  \"openingHoursList\": [\n" +
            "    {\n" +
            "      \"closeTime\": \"20:00\",\n" +
            "      \"openTime\": \"08:00\",\n" +
            "      \"weekDay\": \"MONDAY\"\n" +
            "    }\n" +
            "  ],\n" +
            "  \"placeName\": \"Форум Львів\"\n" +
            "}";

        this.mockMvc.perform(post(placeLink + "/v2/save")
            .contentType(MediaType.APPLICATION_JSON)
            .principal(principal)
            .content(json))
            .andExpect(status().isOk());
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
            .findAll(pageable, "test@gmail.com");
    }
}
