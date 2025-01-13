package greencity.controller;

import greencity.service.AIService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.annotation.Import;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.security.Principal;
import java.util.Locale;
import static greencity.ModelUtils.getPrincipal;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@Import(AIController.class)
class AIControllerTest {
    @Mock
    private AIService aiService;
    @InjectMocks
    private AIController aiController;
    private MockMvc mockMvc;
    private Principal principal = getPrincipal();

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(aiController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void forecastReturnsForecastFromAIServiceTest() throws Exception {
        Locale testLocale = Locale.forLanguageTag("en");

        mockMvc.perform(get("/ai/forecast")
            .principal(principal)
            .locale(testLocale))
            .andExpect(status().isOk());

        verify(aiService, times(1)).getForecast(any(), eq(testLocale.getDisplayLanguage()));
    }

    @Test
    void creatingEcoNewsReturnsEcoNewsFromAIServiceTest() throws Exception {
        Locale testLocale = Locale.forLanguageTag("en");

        mockMvc.perform(get("/ai/generate/eco-news")
            .principal(principal)
            .locale(testLocale))
            .andExpect(status().isOk());

        verify(aiService, times(1)).getNews(eq(testLocale.getDisplayLanguage()), any());
    }
}