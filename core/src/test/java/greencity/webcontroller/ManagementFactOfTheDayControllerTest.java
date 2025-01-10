package greencity.webcontroller;

import greencity.constant.ErrorMessage;
import greencity.dto.PageableDto;
import greencity.dto.factoftheday.FactOfTheDayDTO;
import greencity.dto.factoftheday.FactOfTheDayPostDTO;
import greencity.dto.language.LanguageDTO;
import greencity.exception.exceptions.NotUpdatedException;
import greencity.service.FactOfTheDayService;
import greencity.service.LanguageService;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class ManagementFactOfTheDayControllerTest {

    private static final String managementFactOfTheDayLink = "/management/factoftheday";

    private MockMvc mockMvc;
    @InjectMocks
    private ManagementFactOfTheDayController factOfTheDayController;
    @Mock
    private FactOfTheDayService factOfTheDayService;
    @Mock
    private LanguageService languageService;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(factOfTheDayController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void getAllFactsOfTheDayTest() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<FactOfTheDayDTO> factOfTheDayDTOS = Collections.singletonList(new FactOfTheDayDTO());
        PageableDto<FactOfTheDayDTO> allFactsOfTheDay = new PageableDto<>(factOfTheDayDTOS, 1, 0, 1);
        when(factOfTheDayService.getAllFactsOfTheDay(pageable)).thenReturn(allFactsOfTheDay);

        List<LanguageDTO> languageDTOS = Collections.emptyList();
        when(languageService.getAllLanguages()).thenReturn(languageDTOS);

        mockMvc.perform(get(managementFactOfTheDayLink + "?page=0&size=10"))
            .andExpect(view().name("core/management_fact_of_the_day"))
            .andExpect(model().attribute("pageable", allFactsOfTheDay))
            .andExpect(model().attribute("languages", languageDTOS))
            .andExpect(status().isOk());

        verify(factOfTheDayService).getAllFactsOfTheDay(pageable);
        verify(languageService).getAllLanguages();
    }

    @Test
    void findAllWithoutQueryTest() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<FactOfTheDayDTO> factOfTheDayDTOS = Collections.singletonList(new FactOfTheDayDTO());
        PageableDto<FactOfTheDayDTO> allFactsOfTheDay = new PageableDto<>(factOfTheDayDTOS, 1, 0, 1);
        when(factOfTheDayService.getAllFactsOfTheDay(pageable)).thenReturn(allFactsOfTheDay);

        List<LanguageDTO> languageDTOS = Collections.emptyList();
        when(languageService.getAllLanguages()).thenReturn(languageDTOS);

        mockMvc.perform(get(managementFactOfTheDayLink + "/findAll?page=0&size=10"))
            .andExpect(view().name("core/management_fact_of_the_day"))
            .andExpect(model().attribute("pageable", allFactsOfTheDay))
            .andExpect(model().attribute("languages", languageDTOS))
            .andExpect(status().isOk());

        verify(factOfTheDayService).getAllFactsOfTheDay(pageable);
        verify(languageService).getAllLanguages();
    }

    @Test
    void findAllWithQueryTest() throws Exception {
        String query = "new query";
        Pageable pageable = PageRequest.of(0, 10);
        List<FactOfTheDayDTO> factOfTheDayDTOS = Collections.singletonList(new FactOfTheDayDTO());
        PageableDto<FactOfTheDayDTO> allFactsOfTheDay = new PageableDto<>(factOfTheDayDTOS, 1, 0, 1);

        List<LanguageDTO> languageDTOS = Collections.emptyList();
        when(factOfTheDayService.searchBy(pageable, query)).thenReturn(allFactsOfTheDay);
        when(languageService.getAllLanguages()).thenReturn(languageDTOS);

        mockMvc.perform(get(managementFactOfTheDayLink + "/findAll?page=0&size=10&query=new query"))
            .andExpect(view().name("core/management_fact_of_the_day"))
            .andExpect(model().attribute("pageable", allFactsOfTheDay))
            .andExpect(model().attribute("languages", languageDTOS))
            .andExpect(model().attribute("query", query))
            .andExpect(status().isOk());

        verify(factOfTheDayService).searchBy(pageable, query);
        verify(languageService).getAllLanguages();
    }

    @Test
    void saveFactOfTheDayTest() throws Exception {
        FactOfTheDayPostDTO factOfTheDayPostDTO = getFactOfTheDayPostDTO();
        String json = """
            {
                "id": "1",
                "name": "New Fact"
            }
            """;

        mockMvc.perform(post(managementFactOfTheDayLink + "/")
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(factOfTheDayService).saveFactOfTheDayAndTranslations(factOfTheDayPostDTO);
    }

    private FactOfTheDayPostDTO getFactOfTheDayPostDTO() {
        FactOfTheDayPostDTO factOfTheDayPostDTO = new FactOfTheDayPostDTO();
        factOfTheDayPostDTO.setName("New Fact");
        factOfTheDayPostDTO.setId(1L);
        return factOfTheDayPostDTO;
    }

    @Test
    void saveFactOfTheDayWithoutIdTest() throws Exception {
        mockMvc.perform(post(managementFactOfTheDayLink + "/")
            .content("{\"name\": \"testName\"}")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(factOfTheDayService, never()).saveFactOfTheDayAndTranslations(any(FactOfTheDayPostDTO.class));
    }

    @Test
    void updateFactOfTheDayTest() throws Exception {
        FactOfTheDayPostDTO factOfTheDayPostDTO = getFactOfTheDayPostDTO();
        String json = """
            {
                "id": "1",
                "name": "New Fact"
            }
            """;

        mockMvc.perform(put(managementFactOfTheDayLink + "/")
            .content(json)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(factOfTheDayService).updateFactOfTheDayAndTranslations(factOfTheDayPostDTO);
    }

    @Test
    void updateFactOfTheDayWithoutIdTest() throws Exception {
        mockMvc.perform(put(managementFactOfTheDayLink + "/")
            .content("{\"name\": \"testName\"}")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(factOfTheDayService, never()).updateFactOfTheDayAndTranslations(any(FactOfTheDayPostDTO.class));
    }

    @Test
    void deleteTest() throws Exception {
        mockMvc.perform(delete(managementFactOfTheDayLink + "/?id=1"))
            .andExpect(status().isOk());

        verify(factOfTheDayService).deleteFactOfTheDayAndTranslations(1L);
    }

    @Test
    void deleteBadRequestTest() {
        when(factOfTheDayService.deleteFactOfTheDayAndTranslations(1L))
            .thenThrow(new NotUpdatedException(ErrorMessage.FACT_OF_THE_DAY_NOT_DELETED));
        Assertions.assertThatThrownBy(() -> mockMvc
            .perform(delete(managementFactOfTheDayLink + "/?id=1")))
            .hasCause(new NotUpdatedException(ErrorMessage.FACT_OF_THE_DAY_NOT_DELETED));

        verify(factOfTheDayService).deleteFactOfTheDayAndTranslations(1L);
    }

    @Test
    void deleteAllTest() throws Exception {
        List<Long> longList = Arrays.asList(1L, 2L);
        mockMvc.perform(delete(managementFactOfTheDayLink + "/deleteAll")
            .content("[1,2]")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(factOfTheDayService).deleteAllFactOfTheDayAndTranslations(longList);
    }

    @Test
    void deleteAllBadRequestTest() {
        List<Long> longList = Arrays.asList(1L, 2L);
        when(factOfTheDayService.deleteAllFactOfTheDayAndTranslations(longList))
            .thenThrow(new NotUpdatedException(ErrorMessage.FACT_OF_THE_DAY_NOT_DELETED));
        Assertions.assertThatThrownBy(() -> mockMvc.perform(delete(managementFactOfTheDayLink + "/deleteAll")
            .content("[1,2]")
            .characterEncoding("utf-8")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON)))
            .hasCause(new NotUpdatedException(ErrorMessage.FACT_OF_THE_DAY_NOT_DELETED));

        verify(factOfTheDayService).deleteAllFactOfTheDayAndTranslations(longList);
    }

    @Test
    void testGetAllFactOfTheDayTags_ReturnsTags() throws Exception {
        mockMvc.perform(get(managementFactOfTheDayLink + "/tags")
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(factOfTheDayService, times(1)).getAllFactOfTheDayTags();
    }
}
