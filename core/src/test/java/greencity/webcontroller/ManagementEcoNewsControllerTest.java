package greencity.webcontroller;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import greencity.converters.UserArgumentResolver;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.dto.econews.EcoNewsDto;
import greencity.dto.econews.EcoNewsDtoManagement;
import greencity.dto.econews.EcoNewsViewDto;
import greencity.dto.tag.TagDto;
import greencity.dto.user.UserVO;
import greencity.converters.ZonedDateTimeTypeAdapter;
import greencity.service.EcoNewsService;
import greencity.service.TagsService;
import greencity.service.UserService;
import java.security.Principal;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

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
import org.springframework.validation.Validator;
import static greencity.ModelUtils.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagementEcoNewsControllerTest {
    private static final String managementEcoNewsLink = "/management/eco-news";
    @Mock
    EcoNewsService ecoNewsService;
    @InjectMocks
    ManagementEcoNewsController managementEcoNewsController;
    private MockMvc mockMvc;
    @Mock
    private ModelMapper modelMapper;
    @Mock
    private TagsService tagsService;
    @Mock
    private UserService userService;
    @Mock
    private Validator mockValidator;
    private final Principal principal = getPrincipal();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementEcoNewsController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver(),
                new UserArgumentResolver(userService, modelMapper))
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void getAllEcoNews() throws Exception {
        EcoNewsViewDto ecoNewsViewDto = new EcoNewsViewDto();
        Pageable pageable = PageRequest.of(0, 10);
        List<EcoNewsDto> ecoNewsDtos = Collections.singletonList(new EcoNewsDto());
        PageableAdvancedDto<EcoNewsDto> ecoNewsDtoPageableDto =
            new PageableAdvancedDto<>(ecoNewsDtos, 2, 0, 3, 0, true, true, true, true);
        List<TagDto> tagDtoList = Collections.singletonList(TagDto.builder()
            .id(1L)
            .name("News").build());
        when(tagsService.findAllEcoNewsTags(Locale.getDefault().getLanguage())).thenReturn(tagDtoList);
        when(ecoNewsService.getFilteredDataForManagementByPage(null, pageable, ecoNewsViewDto, Locale.getDefault()))
            .thenReturn(ecoNewsDtoPageableDto);

        this.mockMvc.perform(get(managementEcoNewsLink)
            .param("page", "0")
            .param("size", "10")
            .locale(Locale.getDefault()))
            .andExpect(view().name("core/management_eco_news"))
            .andExpect(model().attribute("pageable", ecoNewsDtoPageableDto))
            .andExpect(model().attribute("fields", ecoNewsViewDto))
            .andExpect(model().attribute("ecoNewsTag", tagDtoList))
            .andExpect(status().isOk());

        verify(ecoNewsService).getFilteredDataForManagementByPage(null, pageable, ecoNewsViewDto, Locale.getDefault());
    }

    @Test
    void delete() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        doNothing().when(ecoNewsService).delete(1L, userVO);
        this.mockMvc.perform(MockMvcRequestBuilders
            .delete(managementEcoNewsLink + "/delete?id=1")
            .principal(principal))
            .andExpect(status().isOk());

        verify(ecoNewsService, times(1)).delete(1L, userVO);
    }

    @Test
    void deleteAll() throws Exception {
        List<Long> longList = Arrays.asList(1L, 2L);
        Gson gson = new Gson();
        String json = gson.toJson(longList);

        this.mockMvc.perform(MockMvcRequestBuilders.delete(managementEcoNewsLink + "/deleteAll")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ecoNewsService).deleteAll(longList);
    }

    @Test
    void getEcoNewsById() throws Exception {
        mockMvc.perform(get(managementEcoNewsLink + "/find/{id}", 1))
            .andExpect(status().isOk());

        verify(ecoNewsService).findDtoByIdAndLanguage(1L, "en");
    }

    @Test
    void getEcoNewsPage() throws Exception {
        ZonedDateTime time = getEcoNewsDto().getCreationDate();
        DateTimeFormatter format = DateTimeFormatter.ofPattern("MMM dd , yyyy");
        when(ecoNewsService.findDtoByIdAndLanguage(1L, "en")).thenReturn(getEcoNewsDto());
        this.mockMvc.perform(get(managementEcoNewsLink + "/1"))
            .andExpect(view().name("core/management_eco_new"))
            .andExpect(model().attribute("econew", getEcoNewsDto()))
            .andExpect(model().attribute("time", time.format(format)))
            .andExpect(model().attribute("ecoNewsTag", tagsService.findAllEcoNewsTags("en")))
            .andExpect(status().isOk());
    }

    @Test
    void saveEcoNews() throws Exception {
        AddEcoNewsDtoRequest addEcoNewsDtoRequest = new AddEcoNewsDtoRequest();
        addEcoNewsDtoRequest.setText("TextTextTextTextTextText");
        addEcoNewsDtoRequest.setTitle("Title");
        addEcoNewsDtoRequest.setTags(Collections.singletonList("News"));
        Gson gson = new Gson();
        String json = gson.toJson(addEcoNewsDtoRequest);
        MockMultipartFile jsonFile =
            new MockMultipartFile("addEcoNewsDtoRequest", "", "application/json", json.getBytes());

        this.mockMvc.perform(multipart(managementEcoNewsLink + "/save")
            .file(jsonFile)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(ecoNewsService, never()).save(addEcoNewsDtoRequest, jsonFile, principal.getName());
    }

    @Test
    void update() throws Exception {
        EcoNewsDtoManagement ecoNewsDtoManagement = new EcoNewsDtoManagement();
        ecoNewsDtoManagement.setId(1L);
        ecoNewsDtoManagement.setTags(Collections.singletonList("News"));
        Gson gson = new GsonBuilder()
            .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeTypeAdapter())
            .create();
        String json = gson.toJson(ecoNewsDtoManagement);
        MockMultipartFile jsonFile =
            new MockMultipartFile("ecoNewsDtoManagement", "", "application/json", json.getBytes());

        this.mockMvc.perform(multipart(managementEcoNewsLink + "/")
            .file(jsonFile)
            .with(mockHttpServletRequest -> {
                mockHttpServletRequest.setMethod("PUT");
                return mockHttpServletRequest;
            })).andExpect(status().isOk());

        verify(ecoNewsService, never()).update(ecoNewsDtoManagement, jsonFile);
    }

    @Test
    void getAllEcoNewsSearchByQueryTest() throws Exception {
        EcoNewsViewDto ecoNewsViewDto = new EcoNewsViewDto();
        Pageable pageable = PageRequest.of(0, 10);
        List<EcoNewsDto> ecoNewsDtos = Collections.singletonList(new EcoNewsDto());
        PageableAdvancedDto<EcoNewsDto> ecoNewsDtoPageableDto =
            new PageableAdvancedDto<>(ecoNewsDtos, 2, 0, 3, 0,
                true, true, true, true);
        String query = "some query";
        List<TagDto> tagDtoList = Collections.singletonList(TagDto.builder()
            .id(1L)
            .name("News").build());
        when(tagsService.findAllEcoNewsTags(Locale.getDefault().getLanguage())).thenReturn(tagDtoList);
        when(ecoNewsService.getFilteredDataForManagementByPage(query, pageable, ecoNewsViewDto, Locale.getDefault()))
            .thenReturn(ecoNewsDtoPageableDto);
        this.mockMvc.perform(get(managementEcoNewsLink + "?query=" + query)
            .param("page", "0")
            .param("size", "10")
            .locale(Locale.getDefault()))
            .andExpect(model().attribute("pageable", ecoNewsDtoPageableDto))
            .andExpect(view().name("core/management_eco_news"))
            .andExpect(model().attribute("query", query))
            .andExpect(model().attribute("fields", ecoNewsViewDto))
            .andExpect(model().attribute("ecoNewsTag", tagDtoList))
            .andExpect(status().isOk());
        verify(ecoNewsService).getFilteredDataForManagementByPage(query, pageable, ecoNewsViewDto, Locale.getDefault());
    }

    @Test
    void getAllEcoNewsSearchByEcoNewsViewDtoTest() throws Exception {
        EcoNewsViewDto ecoNewsViewDto = new EcoNewsViewDto("1", "title", null, null, "2024-08-20", null, null, "true");
        Pageable pageable = PageRequest.of(0, 10);
        List<EcoNewsDto> ecoNewsDtos = Collections.singletonList(new EcoNewsDto());
        PageableAdvancedDto<EcoNewsDto> ecoNewsDtoPageableDto =
            new PageableAdvancedDto<>(ecoNewsDtos, 2, 0, 3, 0,
                true, true, true, true);
        List<TagDto> tagDtoList = Collections.singletonList(TagDto.builder()
            .id(1L)
            .name("News").build());
        when(tagsService.findAllEcoNewsTags(Locale.getDefault().getLanguage())).thenReturn(tagDtoList);
        when(ecoNewsService.getFilteredDataForManagementByPage(null, pageable, ecoNewsViewDto, Locale.getDefault()))
            .thenReturn(ecoNewsDtoPageableDto);
        this.mockMvc.perform(get(managementEcoNewsLink + "?id=1&title=title&startDate=2024-08-20&hidden=true")
            .param("page", "0")
            .param("size", "10")
            .locale(Locale.getDefault()))
            .andExpect(view().name("core/management_eco_news"))
            .andExpect(model().attribute("pageable", ecoNewsDtoPageableDto))
            .andExpect(model().attribute("fields", ecoNewsViewDto))
            .andExpect(model().attribute("ecoNewsTag", tagDtoList))
            .andExpect(status().isOk());
        verify(ecoNewsService).getFilteredDataForManagementByPage(null, pageable, ecoNewsViewDto, Locale.getDefault());
    }

    @Test
    void getAllEcoNewsSorted() throws Exception {
        EcoNewsViewDto ecoNewsViewDto = new EcoNewsViewDto();
        List<EcoNewsDto> ecoNewsDtos = Collections.singletonList(new EcoNewsDto());
        PageableAdvancedDto<EcoNewsDto> ecoNewsDtoPageableDto =
            new PageableAdvancedDto<>(ecoNewsDtos, 2, 0, 3, 0, true, true, true, true);
        List<TagDto> tagDtoList = Collections.singletonList(TagDto.builder()
            .id(1L)
            .name("News").build());
        when(tagsService.findAllEcoNewsTags(Locale.getDefault().getLanguage())).thenReturn(tagDtoList);
        when(ecoNewsService.getFilteredDataForManagementByPage(eq(""), any(Pageable.class), eq(ecoNewsViewDto),
            eq(Locale.getDefault())))
            .thenReturn(ecoNewsDtoPageableDto);

        String sortModel = "id,ASC&sort=text,DESC";
        this.mockMvc.perform(get(managementEcoNewsLink + "?sort=" + sortModel)
            .param("page", "0")
            .param("size", "10")
            .param("query", "")
            .locale(Locale.getDefault()))
            .andExpect(view().name("core/management_eco_news"))
            .andExpect(model().attribute("pageable", ecoNewsDtoPageableDto))
            .andExpect(model().attribute("fields", ecoNewsViewDto))
            .andExpect(model().attribute("ecoNewsTag", tagDtoList))
            .andExpect(model().attribute("sortModel", sortModel))
            .andExpect(status().isOk());

        verify(ecoNewsService).getFilteredDataForManagementByPage(eq(""), any(), eq(ecoNewsViewDto),
            eq(Locale.getDefault()));
    }

    @Test
    void getAllEcoNewsTagTest() throws Exception {
        this.mockMvc.perform(get(managementEcoNewsLink + "/tags")).andExpect(status().isOk());
        verify(tagsService).findAllEcoNewsTags("en");
    }

    @Test
    void getAllEcoNewsSearchByForm() throws Exception {
        EcoNewsDto ecoNewsDto = new EcoNewsDto();
        ecoNewsDto.setCreationDate(ZonedDateTime.now());
        when(ecoNewsService.findDtoByIdAndLanguage(1L, "en")).thenReturn(ecoNewsDto);
        this.mockMvc.perform(get(managementEcoNewsLink + "/1")
            .param("page", "0")
            .param("size", "10"))
            .andExpect(view().name("core/management_eco_new"))
            .andExpect(status().isOk());
        verify(ecoNewsService).findDtoByIdAndLanguage(anyLong(), anyString());
    }

    @Test
    void getLikesByEcoNewsId() throws Exception {
        mockMvc.perform(get(managementEcoNewsLink + "/1/likes"))
            .andExpect(status().isOk());
        verify(ecoNewsService).findUsersWhoLikedPost(1L);
    }

    @Test
    void hide() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        doNothing().when(ecoNewsService).setHiddenValue(1L, userVO, true);
        this.mockMvc.perform(MockMvcRequestBuilders
            .patch(managementEcoNewsLink + "/hide?id=1")
            .principal(principal))
            .andExpect(status().isOk());

        verify(ecoNewsService, times(1)).setHiddenValue(1L, userVO, true);
    }

    @Test
    void show() throws Exception {
        UserVO userVO = getUserVO();
        when(userService.findByEmail(anyString())).thenReturn(userVO);
        doNothing().when(ecoNewsService).setHiddenValue(1L, userVO, false);
        this.mockMvc.perform(MockMvcRequestBuilders
            .patch(managementEcoNewsLink + "/show?id=1")
            .principal(principal))
            .andExpect(status().isOk());

        verify(ecoNewsService, times(1)).setHiddenValue(1L, userVO, false);
    }
}
