package greencity.webcontroller;

import com.google.gson.Gson;
import greencity.dto.PageableDto;
import greencity.dto.socialnetwork.SocialNetworkImageRequestDTO;
import greencity.dto.socialnetwork.SocialNetworkImageResponseDTO;
import greencity.service.SocialNetworkImageService;
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
@MockitoSettings(strictness = Strictness.LENIENT)
class ManagementSocialNetworkImagesControllerTest {
    private static final String managementSocialNetworkImagesLink = "/management/socialnetworkimages";

    private MockMvc mockMvc;

    @Mock
    SocialNetworkImageService socialNetworkImageService;

    @InjectMocks
    ManagementSocialNetworkImagesController managementSocialNetworkImagesController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(managementSocialNetworkImagesController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void getAllSocialNetworkImages() throws Exception {
        Pageable pageable = PageRequest.of(0, 10);
        List<SocialNetworkImageResponseDTO> socialNetworkImageResponseDTOS =
            Collections.singletonList(new SocialNetworkImageResponseDTO());
        PageableDto<SocialNetworkImageResponseDTO> socialNetworkImageResponsePageableDto =
            new PageableDto<>(socialNetworkImageResponseDTOS, 2, 0, 3);
        when(socialNetworkImageService.findAll(pageable)).thenReturn(socialNetworkImageResponsePageableDto);

        this.mockMvc.perform(get(managementSocialNetworkImagesLink)
            .param("page", "0")
            .param("size", "10"))
            .andExpect(view().name("core/management_social_network_images"))
            .andExpect(model().attribute("pageable", socialNetworkImageResponsePageableDto))
            .andExpect(status().isOk());

        verify(socialNetworkImageService).findAll(pageable);
    }

    @Test
    void save() throws Exception {
        SocialNetworkImageRequestDTO socialNetworkImageRequestDTO = new SocialNetworkImageRequestDTO();
        Gson gson = new Gson();
        String json = gson.toJson(socialNetworkImageRequestDTO);
        MockMultipartFile jsonFile =
            new MockMultipartFile("socialNetworkImageRequestDTO", "", "application/json", json.getBytes());

        this.mockMvc.perform(multipart(managementSocialNetworkImagesLink + "/")
            .file(jsonFile)
            .accept(MediaType.APPLICATION_JSON)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(socialNetworkImageService, never()).save(socialNetworkImageRequestDTO, jsonFile);
    }

    @Test
    void delete() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.delete(managementSocialNetworkImagesLink + "/delete?id=1"))
            .andExpect(status().isOk());

        verify(socialNetworkImageService, times(1)).delete(1L);
    }

    @Test
    void deleteAll() throws Exception {
        List<Long> longList = Arrays.asList(1L, 2L);
        Gson gson = new Gson();
        String json = gson.toJson(longList);

        this.mockMvc.perform(MockMvcRequestBuilders.delete(managementSocialNetworkImagesLink + "/deleteAll")
            .content(json)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(socialNetworkImageService).deleteAll(longList);
    }

    @Test
    void getEcoNewsById() throws Exception {
        this.mockMvc.perform(get(managementSocialNetworkImagesLink + "/find?id=1"))
            .andExpect(status().isOk());

        verify(socialNetworkImageService).findDtoById(1L);
    }

    @Test
    void update() throws Exception {
        SocialNetworkImageResponseDTO socialNetworkImageResponseDTO = new SocialNetworkImageResponseDTO();
        Gson gson = new Gson();
        String json = gson.toJson(socialNetworkImageResponseDTO);
        MockMultipartFile jsonFile =
            new MockMultipartFile("socialNetworkImageResponseDTO", "", "application/json", json.getBytes());

        this.mockMvc.perform(multipart(managementSocialNetworkImagesLink + "/")
            .file(jsonFile)
            .with(new RequestPostProcessor() {
                @Override
                public MockHttpServletRequest postProcessRequest(MockHttpServletRequest mockHttpServletRequest) {
                    mockHttpServletRequest.setMethod("PUT");
                    return mockHttpServletRequest;
                }
            })).andExpect(status().isOk());

        verify(socialNetworkImageService, never()).update(socialNetworkImageResponseDTO, jsonFile);
    }
}
