package greencity.controller;

import greencity.enums.TagType;
import greencity.service.TagsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import java.util.Locale;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class TagsControllerTest {

    private MockMvc mockMvc;

    @InjectMocks
    private TagsController tagsController;

    @Mock
    private TagsService tagsService;

    @Mock
    private Validator mockValidator;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(tagsController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void findAllEcoNewsTags() throws Exception {
        String tagsLink = "/tags";
        Locale locale = Locale.of("en");
        TagType type = TagType.ECO_NEWS;

        mockMvc.perform(get(tagsLink + "/search?type=" + type))
            .andExpect(status().isOk());

        verify(tagsService).findByTypeAndLanguageCode(type, locale.getLanguage());
    }

    @Test
    void findByTypeTest() throws Exception {
        String tagsLink = "/tags";

        mockMvc.perform(get(tagsLink + "/v2/search?type=" + TagType.ECO_NEWS))
            .andExpect(status().isOk());

        verify(tagsService).findByType(TagType.ECO_NEWS);
    }
}