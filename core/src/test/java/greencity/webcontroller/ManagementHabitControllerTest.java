package greencity.webcontroller;

import com.google.gson.Gson;
import greencity.dto.PageableDto;
import greencity.dto.habit.HabitManagementDto;
import greencity.dto.habitfact.HabitFactVO;
import greencity.dto.language.LanguageDTO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.shoppinglistitem.ShoppingListItemManagementDto;
import greencity.enums.HabitAssignStatus;
import greencity.service.*;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ManagementHabitControllerTest {

    private static final String habitManagementLink = "/management/habits";

    private MockMvc mockMvc;

    @Mock
    private ManagementHabitService managementHabitService;

    @Mock
    private HabitFactService habitFactService;

    @Mock
    private LanguageService languageService;

    @Mock
    private ShoppingListItemService shoppingListItemService;

    @Mock
    private AdviceService adviceService;

    @Mock
    private HabitAssignService habitAssignService;

    @InjectMocks
    ManagementHabitController managementHabitController;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementHabitController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void findAllHabits() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);
        List<HabitManagementDto> habitManagementDtos = Collections.singletonList(new HabitManagementDto());
        PageableDto<HabitManagementDto> habitManagementDtoPageableDto = new PageableDto<>(habitManagementDtos, 4, 0, 3);

        when(managementHabitService.getAllHabitsDto(null, null, null, null,
            null, null, pageable)).thenReturn(habitManagementDtoPageableDto);

        List<LanguageDTO> languageDtos = Collections.singletonList(new LanguageDTO());

        when(languageService.getAllLanguages()).thenReturn(languageDtos);

        this.mockMvc.perform(get(habitManagementLink)
            .param("page", "0")
            .param("size", "5"))
            .andExpect(view().name("core/management_user_habits"))
            .andExpect(model().attribute("pageable", habitManagementDtoPageableDto))
            .andExpect(model().attribute("languages", languageDtos))
            .andExpect(status().isOk());

        verify(managementHabitService).getAllHabitsDto(null, null, null, null, null, null, pageable);
    }

    @Test
    void getHabitById() throws Exception {
        this.mockMvc.perform(get(habitManagementLink + "/find?id=1"))
            .andExpect(status().is4xxClientError());
    }

    @Test
    void getHabitByIdPage() throws Exception {
        Pageable pageable = PageRequest.of(0, 5);

        PageableDto<HabitFactVO> hfacts = habitFactService.getAllHabitFactsVO(pageable);
        List<ShoppingListItemManagementDto> hshops = shoppingListItemService.getShoppingListByHabitId(1L);
        HabitManagementDto habit = managementHabitService.getById(1L);
        List<LanguageTranslationDTO> hadvices = adviceService.getAllByHabitIdAndLanguage(1L, "en");
        Long acquired = habitAssignService.getNumberHabitAssignsByHabitIdAndStatus(1L, HabitAssignStatus.ACQUIRED);
        Long inProgress = habitAssignService.getNumberHabitAssignsByHabitIdAndStatus(1L, HabitAssignStatus.INPROGRESS);
        Long canceled = habitAssignService.getNumberHabitAssignsByHabitIdAndStatus(1L, HabitAssignStatus.CANCELLED);

        this.mockMvc.perform(get(habitManagementLink + "/1")
            .param("page", "0")
            .param("size", "5"))
            .andExpect(view().name("core/management_user_habit"))
            .andExpect(model().attribute("hfacts", hfacts))
            .andExpect(model().attribute("hshops", hshops))
            .andExpect(model().attribute("habit", habit))
            .andExpect(model().attribute("hadvices", hadvices))
            .andExpect(model().attribute("acquired", acquired))
            .andExpect(model().attribute("inProgress", inProgress))
            .andExpect(model().attribute("canceled", canceled))
            .andExpect(status().isOk());
    }

    @Test
    void save() throws Exception {
        HabitManagementDto habitManagementDto = new HabitManagementDto();
        Gson gson = new Gson();
        String json = gson.toJson(habitManagementDto);

        MockMultipartFile jsonFile =
            new MockMultipartFile("habitManagementDto", "", "application/json", json.getBytes());
        this.mockMvc.perform(multipart(habitManagementLink + "/save")
            .file(jsonFile)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(managementHabitService, never()).saveHabitAndTranslations(habitManagementDto, jsonFile);
    }

    @Test
    void update() throws Exception {
        HabitManagementDto habitManagementDto = new HabitManagementDto();
        Gson gson = new Gson();
        String json = gson.toJson(habitManagementDto);

        MockMultipartFile jsonFile =
            new MockMultipartFile("habitManagementDto", "", "application/json", json.getBytes());

        this.mockMvc.perform(multipart(habitManagementLink + "/update")
            .file(jsonFile)
            .with(new RequestPostProcessor() {
                @Override
                public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
                    mockHttpServletRequest.setMethod("PUT");
                    return mockHttpServletRequest;
                }
            })).andExpect(status().isOk());

        verify(managementHabitService, never()).update(habitManagementDto, jsonFile);
    }

    @Test
    void delete() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete(habitManagementLink + "/delete?id=1"))
            .andExpect(status().isOk());

        verify(managementHabitService).delete(1L);
    }

    @Test
    void deleteAll() throws Exception {
        List<Long> idsToDelete = Arrays.asList(1L, 2L);
        Gson gson = new Gson();
        String json = gson.toJson(idsToDelete);
        this.mockMvc.perform(MockMvcRequestBuilders.delete(habitManagementLink + "/deleteAll")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(managementHabitService).deleteAll(idsToDelete);
    }
}
