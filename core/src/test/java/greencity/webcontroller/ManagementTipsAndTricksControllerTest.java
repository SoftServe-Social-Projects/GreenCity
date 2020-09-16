package greencity.webcontroller;

import com.google.gson.Gson;
import greencity.dto.PageableDto;
import greencity.dto.tipsandtricks.TipsAndTricksDtoManagement;
import greencity.dto.tipsandtricks.TipsAndTricksDtoRequest;
import greencity.dto.tipsandtricks.TipsAndTricksDtoResponse;
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
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagementTipsAndTricksControllerTest {
    private static final String managementTipsAndTricksLink = "/management/tipsandtricks";

    private MockMvc mockMvc;

    @InjectMocks
    private ManagementTipsAndTricksController managementTipsAndTricksController;

    @Mock
    TipsAndTricksService tipsAndTricksService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementTipsAndTricksController)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

    }

    @Test
    void findAll() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<TipsAndTricksDtoResponse> tipsAndTricksDtoResponses = Collections.singletonList(new TipsAndTricksDtoResponse());
        PageableDto<TipsAndTricksDtoResponse> tipsAndTricksDtoResponsePageableDto = new PageableDto<>(tipsAndTricksDtoResponses, 2, 0, 3);
        when(tipsAndTricksService.findAll(pageable)).thenReturn(tipsAndTricksDtoResponsePageableDto);

        this.mockMvc.perform(get(managementTipsAndTricksLink)
                .param("page", "0")
                .param("size", "10"))
                .andExpect(view().name("core/management_tips_and_tricks"))
                .andExpect(model().attribute("pageable", tipsAndTricksDtoResponsePageableDto))
                .andExpect(status().isOk());

        verify(tipsAndTricksService).findAll(pageable);

    }

    @Test
    void getTipsAndTricksById() throws Exception {
        this.mockMvc.perform(get(managementTipsAndTricksLink + "/find?id=1"))
                .andExpect(status().isOk());

        verify(tipsAndTricksService).findManagementDtoById(1L);

    }

    @Test
    void delete() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete(managementTipsAndTricksLink + "/?id=1"))
                .andExpect(status().isOk());

        verify(tipsAndTricksService, times(1)).delete(1L);
    }

    @Test
    void deleteAll() throws Exception {
        List<Long> longList = Arrays.asList(1L, 2L);
        Gson gson = new Gson();
        String json = gson.toJson(longList);

        this.mockMvc.perform(MockMvcRequestBuilders.delete(managementTipsAndTricksLink + "/deleteAll")
                .content(json)
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(tipsAndTricksService).deleteAll(longList);

    }

    @Test
    void update() throws Exception{
        TipsAndTricksDtoManagement tipsAndTricksDtoManagement = new TipsAndTricksDtoManagement();
        Gson gson = new Gson();
        String json = gson.toJson(tipsAndTricksDtoManagement);
        MockMultipartFile jsonFile = new MockMultipartFile("tipsAndTricksDtoManagement", "", "application/json", json.getBytes());

        this.mockMvc.perform(multipart(managementTipsAndTricksLink + "/")
                .file(jsonFile)
                .with(new RequestPostProcessor() {
            @Override
            public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
                mockHttpServletRequest.setMethod("PUT");
                return mockHttpServletRequest;
            }
        })).andExpect(status().isOk());

        verify(tipsAndTricksService, never()).update(tipsAndTricksDtoManagement, jsonFile);
    }

    @Test
    void save() throws Exception {
        Principal principal = Mockito.mock(Principal.class);
        TipsAndTricksDtoRequest tipsAndTricksDtoRequest = new TipsAndTricksDtoRequest();
        Gson gson = new Gson();
        String json = gson.toJson(tipsAndTricksDtoRequest);
        MockMultipartFile jsonFile = new MockMultipartFile("tipsAndTricksDtoRequest", "", "application/json", json.getBytes());

        this.mockMvc.perform(multipart(managementTipsAndTricksLink + "/")
                .file(jsonFile)
                .principal(principal)
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        verify(tipsAndTricksService, never()).save(tipsAndTricksDtoRequest, jsonFile, principal.getName());
    }
}