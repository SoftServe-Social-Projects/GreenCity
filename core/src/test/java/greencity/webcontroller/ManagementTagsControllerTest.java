package greencity.webcontroller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.ModelUtils;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.language.LanguageDTO;
import greencity.dto.tag.TagPostDto;
import greencity.dto.tag.TagVO;
import greencity.dto.tag.TagViewDto;
import greencity.service.LanguageService;
import greencity.service.TagsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.BindingResult;
import org.springframework.validation.Validator;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagementTagsControllerTest {
    private static final String managementTagsLink = "/management/tags";

    private MockMvc mockMvc;

    @Mock
    private TagsService tagsService;

    @Mock
    private LanguageService languageService;

    @Mock
    private Validator validator;

    @MockBean
    private BindingResult bindingResult;

    @InjectMocks
    private ManagementTagsController tagsController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(tagsController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(validator)
            .build();
    }

    @Test
    void findAll() throws Exception {
        int page = 0;
        int size = 10;
        String language = "ua";
        Pageable pageable = PageRequest.of(page, size);
        List<LanguageDTO> languages = Collections.singletonList(ModelUtils.getLanguageDTO());
        PageableAdvancedDto<TagVO> tags = ModelUtils.getPageableAdvancedDtoForTag();

        when(tagsService.findAll(pageable, null)).thenReturn(tags);
        when(languageService.getAllLanguages()).thenReturn(languages);

        mockMvc.perform(get(managementTagsLink + "?lang=" + language +
            "&page=" + page + "&size=" + size))
            .andExpect(view().name("core/management_tags"))
            .andExpect(model().attribute("tags", tags))
            .andExpect(model().attribute("languages", languages));

        verify(tagsService).findAll(pageable, null);
        verify(languageService).getAllLanguages();
    }

    @Test
    void save() throws Exception {
        TagPostDto tagPostDto = ModelUtils.getTagPostDto();
        String tagPostDtoAsJson = objectMapper.writeValueAsString(tagPostDto);

        mockMvc.perform(post(managementTagsLink)
            .content(tagPostDtoAsJson)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(tagsService).save(tagPostDto);
    }

    @Test
    void findById() throws Exception {
        Long id = 3L;
        TagVO tagVO = ModelUtils.getTagVO();

        when(tagsService.findById(id)).thenReturn(tagVO);

        mockMvc.perform(get(managementTagsLink + "/" + id))
            .andExpect(status().isOk());
    }

    @Test
    void bulkDelete() throws Exception {
        List<Long> ids = Arrays.asList(3L, 5L, 7L);
        String idsAsString = objectMapper.writeValueAsString(ids);

        mockMvc.perform(delete(managementTagsLink)
            .contentType(MediaType.APPLICATION_JSON)
            .content(idsAsString))
            .andExpect(status().isOk());

        verify(tagsService).bulkDelete(ids);
    }

    @Test
    void deleteById() throws Exception {
        Long id = 3L;

        mockMvc.perform(delete(managementTagsLink + "/" + id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(tagsService).deleteById(id);
    }

    @Test
    void updateTag() throws Exception {
        Long id = 3L;
        TagPostDto tagPostDto = ModelUtils.getTagPostDto();
        String tagPostDtoAsJson = objectMapper.writeValueAsString(tagPostDto);

        mockMvc.perform(put(managementTagsLink + "/" + id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(tagPostDtoAsJson))
            .andExpect(status().isOk());

        verify(tagsService).update(tagPostDto, id);
    }

    @Test
    void search() throws Exception {
        int page = 0;
        int size = 10;
        String language = "ua";
        Pageable pageable = PageRequest.of(page, size);
        List<LanguageDTO> languages = Collections.singletonList(ModelUtils.getLanguageDTO());
        PageableAdvancedDto<TagVO> tags = ModelUtils.getPageableAdvancedDtoForTag();
        TagViewDto tagViewDto = ModelUtils.getTagViewDto();
        String tagViewDtoAsJson = objectMapper.writeValueAsString(tagViewDto);

        when(tagsService.search(pageable, tagViewDto)).thenReturn(tags);
        when(languageService.getAllLanguages()).thenReturn(languages);

        mockMvc.perform(post(managementTagsLink + "/search?lang=" + language +
            "&page=" + page + "&size=" + size)
            .contentType(MediaType.APPLICATION_JSON)
            .content(tagViewDtoAsJson))
            .andExpect(status().isOk());
    }
}