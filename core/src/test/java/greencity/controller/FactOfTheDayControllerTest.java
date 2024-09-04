package greencity.controller;

import greencity.service.FactOfTheDayService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.Validator;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class FactOfTheDayControllerTest {
    private static final String factOfTheDayLink = "/fact-of-the-day";

    private MockMvc mockMvc;
    @InjectMocks
    private FactOfTheDayController factOfTheDayController;
    @Mock
    private FactOfTheDayService factOfTheDayService;

    @Mock
    private Validator mockValidator;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(factOfTheDayController)
            .setValidator(mockValidator)
            .build();
    }

    @Test
    void getRandomFactOfTheDayTest() throws Exception {
        mockMvc.perform(get(factOfTheDayLink + "/random"))
            .andExpect(status().isOk());
        verify(factOfTheDayService).getRandomFactOfTheDayByLanguage("en");
    }
}
