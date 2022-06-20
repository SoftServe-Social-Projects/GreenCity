package greencity.webcontroller;

import com.azure.core.http.ContentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.AchievementDto;
import greencity.dto.achievement.AchievementManagementDto;
import greencity.dto.achievement.AchievementPostDto;
import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.language.LanguageDTO;
import greencity.service.AchievementCategoryService;
import greencity.service.AchievementService;
import greencity.service.LanguageService;
import org.apache.poi.openxml4j.opc.ContentTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMultipartHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagementAchievementControllerTest {

    private final String link = "/management/achievement";

    private MockMvc mockMvc;

    private ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AchievementService achievementService;

    @Mock
    private AchievementCategoryService achievementCategoryService;

    @Mock
    private LanguageService languageService;

    @InjectMocks
    private ManagementAchievementController managementAchievementController;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementAchievementController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void getAllAchievementTest() throws Exception {
        Pageable paging = PageRequest.of(0, 3, Sort.by("id").descending());
        List<AchievementDto> list = Collections.singletonList(ModelUtils.getAchievementDto());
        PageableAdvancedDto<AchievementDto> allAchievements = new PageableAdvancedDto<>(list, 3, 0,
            3, 0, false, true, true, false);
        List<AchievementCategoryVO> achievementCategoryList = Collections.singletonList(new AchievementCategoryVO());
        when(achievementService.findAll(paging)).thenReturn(allAchievements);
        when(achievementCategoryService.findAll()).thenReturn(achievementCategoryList);
        this.mockMvc.perform(get(link)
            .param("page", "0")
            .param("size", "3"))
            .andExpect(model().attribute("pageable", allAchievements))
            .andExpect(model().attribute("categoryList", achievementCategoryList))
            .andExpect(view().name("core/management_achievement"))
            .andExpect(status().isOk());
        verify(achievementService).findAll(paging);
        verify(achievementCategoryService).findAll();
    }

    @Test
    void getAllAchievementSearchByQueryTest() throws Exception {
        Pageable pageable = PageRequest.of(0, 3, Sort.by("id").descending());
        List<AchievementDto> list = Collections.singletonList(new AchievementDto());
        PageableAdvancedDto<AchievementDto> allAchievements = new PageableAdvancedDto<>(list, 3, 0,
            3, 0, false, true, true, false);
        List<AchievementCategoryVO> achievementCategoryList = Collections.singletonList(new AchievementCategoryVO());
        List<LanguageDTO> languages = Collections.singletonList(ModelUtils.getLanguageDTO());
        when(achievementService.searchAchievementBy(pageable, "query")).thenReturn(allAchievements);
        when(achievementCategoryService.findAll()).thenReturn(achievementCategoryList);
        when(languageService.getAllLanguages()).thenReturn(languages);
        this.mockMvc.perform(get(link + "?query=query")
            .param("page", "0")
            .param("size", "3"))
            .andExpect(model().attribute("pageable", allAchievements))
            .andExpect(model().attribute("categoryList", achievementCategoryList))
            .andExpect(model().attribute("languages", languages))
            .andExpect(view().name("core/management_achievement"))
            .andExpect(status().isOk());
        verify(achievementService).searchAchievementBy(pageable, "query");
        verify(achievementCategoryService).findAll();
        verify(languageService).getAllLanguages();
    }

    @Test
    void saveAchievementTest() throws Exception {
        AchievementPostDto achievementPostDto = ModelUtils.getAchievementPostDto();

        MockMultipartFile json =
            new MockMultipartFile("achievementPostDto", "", ContentType.APPLICATION_JSON,
                objectMapper.writeValueAsBytes(achievementPostDto));
        MockMultipartFile file =
            new MockMultipartFile("file", "icon.png", ContentTypes.IMAGE_PNG, (byte[]) null);

        this.mockMvc.perform(MockMvcRequestBuilders.multipart(link)
            .file(json)
            .file(file))
            .andExpect(status().isOk());

        verify(achievementService).save(achievementPostDto, file);
    }

    @Test
    void deleteAchievementByIdTest() throws Exception {
        this.mockMvc.perform(delete(link + "/1")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk());
        verify(achievementService).delete(1L);
    }

    @Test
    void deleteAllTest() throws Exception {
        List<Long> listId = Collections.singletonList(1L);
        String content = objectMapper.writeValueAsString(listId);
        this.mockMvc.perform(delete(link + "/deleteAll")
            .content(content)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(achievementService).deleteAll(listId);
    }

    @Test
    void getAchievementByIdTest() throws Exception {
        this.mockMvc.perform(get(link + "/1")
            .contentType(MediaType.APPLICATION_FORM_URLENCODED))
            .andExpect(status().isOk());
        verify(achievementService).findById(1L);
    }

    @Test
    void updateTest() throws Exception {
        AchievementManagementDto achievementManagementDto = new AchievementManagementDto(1L);

        MockMultipartFile json =
            new MockMultipartFile("achievementManagementDto", "", ContentType.APPLICATION_JSON,
                objectMapper.writeValueAsBytes(achievementManagementDto));
        MockMultipartFile file =
            new MockMultipartFile("file", "icon.png", ContentTypes.IMAGE_PNG, (byte[]) null);

        MockMultipartHttpServletRequestBuilder builder = MockMvcRequestBuilders.multipart(link)
            .file(json)
            .file(file);
        builder.with(request -> {
            request.setMethod(HttpMethod.PUT.name());
            return request;
        });

        this.mockMvc.perform(builder).andExpect(status().isOk());

        verify(achievementService).update(achievementManagementDto, file);
    }
}
