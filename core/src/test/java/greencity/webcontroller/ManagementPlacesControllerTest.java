package greencity.webcontroller;

import greencity.dto.PageableDto;
import greencity.dto.category.CategoryDto;
import greencity.dto.discount.DiscountValueDto;
import greencity.dto.location.LocationAddressAndGeoForUpdateDto;
import greencity.dto.openhours.OpeningHoursDto;
import greencity.dto.place.AdminPlaceDto;
import greencity.dto.place.PlaceUpdateDto;
import greencity.dto.specification.SpecificationNameDto;
import greencity.service.CategoryService;
import greencity.service.PlaceService;
import greencity.service.SpecificationService;

import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MultipartFile;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagementPlacesControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private ManagementPlacesController managementPlacesController;

    @Mock
    private PlaceService placeService;

    @Mock
    private CategoryService categoryService;

    @Mock
    private SpecificationService specificationService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementPlacesController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void getAllPlaces() throws Exception {
        List<AdminPlaceDto> placeDtos = Collections.singletonList(new AdminPlaceDto());
        PageableDto<AdminPlaceDto> adminPlaceDtoPageableDto = new PageableDto<>(placeDtos, 1, 0, 1);
        when(placeService.getFilteredPlacesForAdmin(any(), any())).thenReturn(adminPlaceDtoPageableDto);
        when(categoryService.findAllCategoryDto())
            .thenReturn(Collections.singletonList(new CategoryDto("test", "test", null)));
        when(specificationService.findAllSpecificationDto())
            .thenReturn(Collections.singletonList(new SpecificationNameDto()));

        this.mockMvc.perform(get("/management/places")
            .param("page", "0"))
            .andExpect(view().name("core/management_places"))
            .andExpect(model().attribute("pageable", adminPlaceDtoPageableDto))
            .andExpect(status().isOk());

        verify(placeService).getFilteredPlacesForAdmin(any(), any());
        verify(categoryService).findAllCategoryDto();
        verify(specificationService).findAllSpecificationDto();
    }

    @Test
    void getPlaceByIdTest() throws Exception {
        this.mockMvc.perform(get("/management/places/find?id=1")).andExpect(status().isOk());
        verify(placeService).getInfoForUpdatingById(1L);
    }

    @Test
    void savePlace() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        String json = """
            {
                "placeName": "Тестове місце",
                "locationName": "Смиків, південна 7",
                "status": "APPROVED",
                "categoryName": "Recycling points",
                "discountValues": null,
                "openingHoursList": [
                    {
                        "weekDay": "MONDAY",
                        "openTime": "17:34",
                        "closeTime": "19:34",
                        "breakTime": null
                    }
                ]
            }
            """;

        MockMultipartFile addPlaceDto = new MockMultipartFile(
            "addPlaceDto",
            "",
            "application/json",
            (json)
                .getBytes());

        this.mockMvc.perform(multipart("/management/places/")
            .file(addPlaceDto)
            .principal(principal)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isOk());

        verify(placeService).addPlaceFromUi(any(), any(), any());
    }

    @Test
    void updatePlaceTest() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        Mockito.when(principal.getName()).thenReturn("testUser");

        PlaceUpdateDto placeUpdateDto = getPlaceUpdateDto();
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        String json = objectMapper.writeValueAsString(placeUpdateDto);

        MockMultipartFile placeUpdateDtoPart = new MockMultipartFile(
            "placeUpdateDto",
            "",
            MediaType.APPLICATION_JSON_VALUE,
            json.getBytes(StandardCharsets.UTF_8));

        MockMultipartFile imagePart = new MockMultipartFile(
            "images",
            "test-image.jpg",
            MediaType.IMAGE_JPEG_VALUE,
            "image-content".getBytes(StandardCharsets.UTF_8));

        this.mockMvc.perform(multipart(HttpMethod.PUT, "/management/places/")
            .file(placeUpdateDtoPart)
            .file(imagePart)
            .principal(principal)
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE)
            .characterEncoding("UTF-8"))
            .andExpect(status().isOk());

        verify(placeService).updateFromUI(eq(placeUpdateDto), any(MultipartFile[].class), anyString());
    }

    private PlaceUpdateDto getPlaceUpdateDto() {
        return PlaceUpdateDto.builder()
            .id(1L)
            .name("Test Place")
            .location(new LocationAddressAndGeoForUpdateDto(
                "Test Address",
                50.45,
                30.52,
                "Тестова адреса"))
            .category(new CategoryDto("Food", "Їжа", null))
            .openingHoursList(Set.of(new OpeningHoursDto(
                LocalTime.of(10, 0),
                LocalTime.of(22, 0),
                DayOfWeek.FRIDAY,
                null)))
            .discountValues(Set.of(new DiscountValueDto(10, new SpecificationNameDto("kdf"))))
            .build();
    }

    @Test
    void updatePlaceWithoutIdTest() throws Exception {
        String json = """
                {
                    "name": "test",
                    "category": {
                        "name": "Food"
                    },
                    "discountValues": [{"value": 10}],
                    "location": {
                        "address": "address",
                        "lat": 111.1,
                        "lng": 111.1,
                        "addressUa": "адреса"
                    },
                    "openingHoursList": [{"dayOfWeek": "MONDAY", "openTime": "08:00", "closeTime": "22:00"}]
                }
            """;

        mockMvc.perform(multipart("/management/places/")
            .file(new MockMultipartFile(
                "placeUpdateDto",
                "placeUpdateDto.json",
                MediaType.APPLICATION_JSON_VALUE,
                json.getBytes()))
            .contentType(MediaType.MULTIPART_FORM_DATA_VALUE))
            .andExpect(status().isBadRequest());

        verify(placeService, never()).updateFromUI(any(PlaceUpdateDto.class), any(), any());
    }

    @Test
    void delete() throws Exception {
        mockMvc.perform(MockMvcRequestBuilders.delete("/management/places/?id=1"))
            .andExpect(status().isOk());

        verify(placeService).deleteById(1L);

    }

    @Test
    void deleteAll() throws Exception {
        List<Long> listId = Arrays.asList(1L, 2L);
        mockMvc.perform(MockMvcRequestBuilders.delete("/management/places/deleteAll")
            .content("[1,2]")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(placeService).bulkDelete(listId);

    }
}
