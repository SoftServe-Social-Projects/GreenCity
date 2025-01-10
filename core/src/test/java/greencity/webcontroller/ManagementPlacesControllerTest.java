package greencity.webcontroller;

import greencity.dto.PageableDto;
import greencity.dto.category.CategoryDto;
import greencity.dto.location.LocationAddressAndGeoForUpdateDto;
import greencity.dto.place.AdminPlaceDto;
import greencity.dto.place.PlaceUpdateDto;
import greencity.dto.specification.SpecificationNameDto;
import greencity.service.CategoryService;
import greencity.service.PlaceService;
import greencity.service.SpecificationService;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
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
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
                "locationName": "смиків, південна 7",
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
        PlaceUpdateDto placeUpdateDto = getPlaceUpdateDto();
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(placeUpdateDto);
        mockMvc.perform(put("/management/places/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(placeService).update(placeUpdateDto);

    }

    @Test
    void updatePlaceWithoutIdTest() throws Exception {
        String json = """
                {
                    "name": "test",
                    "category": {
                        "name": "Food"
                    },
                    "discountValues": null,
                    "location": {
                        "address": "address",
                        "lat": 111.1,
                        "lng": 111.1
                    }
                }
            """;

        mockMvc.perform(put("/management/places/")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(placeService, never()).update(any(PlaceUpdateDto.class));
    }

    private PlaceUpdateDto getPlaceUpdateDto() {
        PlaceUpdateDto placeUpdateDto = new PlaceUpdateDto();
        placeUpdateDto.setId(1L);
        placeUpdateDto.setName("test");
        placeUpdateDto.setCategory(new CategoryDto("Food", "test", null));
        placeUpdateDto.setLocation(new LocationAddressAndGeoForUpdateDto("address", 111.1, 111.1));

        return placeUpdateDto;
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
