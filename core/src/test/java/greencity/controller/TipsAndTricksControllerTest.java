package greencity.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.tipsandtricks.TipsAndTricksDtoRequest;
import greencity.service.TagsService;
import greencity.service.TipsAndTricksService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class TipsAndTricksControllerTest {
    private static final String tipsAndTricksLink = "/tipsandtricks";
    private MockMvc mockMvc;
    @InjectMocks
    private TipsAndTricksController tipsAndTricksController;
    @Mock
    private TipsAndTricksService tipsAndTricksService;
    @Mock
    private TagsService tagService;

    @BeforeEach
    public void setUp() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(tipsAndTricksController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void saveTest() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        when(principal.getName()).thenReturn("Jane.Smith@gmail.com");
        String json = "{\n" +
            "\"title\": \"title\",\n" +
            " \"tags\": [\"news\"],\n" +
            " \"text\": \"content content content\", \n" +
            "\"source\": \"\",\n" +
            " \"image\": null\n" +
            "}";
        MockMultipartFile jsonFile =
            new MockMultipartFile("tipsAndTricksDtoRequest", "", "application/json", json.getBytes());

        this.mockMvc.perform(multipart(tipsAndTricksLink)
            .file(jsonFile)
            .principal(principal)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        ObjectMapper mapper = new ObjectMapper();
        TipsAndTricksDtoRequest tipsAndTricksDtoRequest = mapper.readValue(json, TipsAndTricksDtoRequest.class);

        verify(tipsAndTricksService, times(1))
            .save(eq(tipsAndTricksDtoRequest), isNull(), eq("Jane.Smith@gmail.com"));
    }

    @Test
    void saveBadRequestTest() throws Exception {
        this.mockMvc.perform(post(tipsAndTricksLink)
            .content("{}")
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());
    }

    @Test
    void getTipsAndTricksByIdTest() throws Exception {
        this.mockMvc.perform(get(tipsAndTricksLink + "/{id}", 1))
            .andExpect(status().isOk());

        verify(tipsAndTricksService, times(1))
            .findDtoById(eq(1L));
    }

    @Test
    void findAllTest() throws Exception {
        int pageNumber = 1;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);

        this.mockMvc.perform(get(tipsAndTricksLink + "?page=1"))
            .andExpect(status().isOk());

        verify(tipsAndTricksService, times(1))
            .findAll(eq(pageable));
    }

    @Test
    void deleteTest() throws Exception {
        this.mockMvc.perform(delete(tipsAndTricksLink + "/{id}", 1))
            .andExpect(status().isOk());

        verify(tipsAndTricksService, times(1))
            .delete(eq(1L));
    }

    @Test
    void getTipsAndTricksTest() throws Exception {
        int pageNumber = 1;
        int pageSize = 20;
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        List<String> tags = Collections.singletonList("education");

        this.mockMvc.perform(get(tipsAndTricksLink + "/tags?page=1&tags=education"))
            .andExpect(status().isOk());

        verify(tipsAndTricksService, times(1))
            .find(eq(pageable), eq(tags));
    }

    @Test
    void findAllTipsAndTricksTagsTest() throws Exception {
        this.mockMvc.perform(get(tipsAndTricksLink + "/tags/all"))
            .andExpect(status().isOk());

        verify(tagService).findAllTipsAndTricksTags();
    }
}