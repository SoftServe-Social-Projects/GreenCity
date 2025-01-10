package greencity.webcontroller;

import greencity.service.GraphService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GraphControllerTest {

    private static final String link = "/management/displayGraph";

    private final List<String> months =
        Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec");

    private MockMvc mockMvc;

    @Mock
    private GraphService graphService;

    @InjectMocks
    private GraphController graphController;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(graphController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void displayGraphTest() throws Exception {
        Map<String, Integer> usersByCities = new HashMap<>();
        Map<Integer, Long> generalRegistrationStatistics = new HashMap<>();
        generalRegistrationStatistics.put(1, 2L);
        usersByCities.put("city", 1);
        when(graphService.getGeneralStatisticsForAllUsersByCities()).thenReturn(usersByCities);
        when(graphService.getRegistrationStatistics()).thenReturn(generalRegistrationStatistics);
        this.mockMvc.perform(get(link))
            .andExpect(model().attribute("months", months))
            .andExpect(model().attribute("usersByCities", usersByCities))
            .andExpect(model().attribute("generalRegistrationStatistics", generalRegistrationStatistics))
            .andExpect(view().name("core/management_general_statistics_for_users"))
            .andExpect(status().isOk());
        verify(graphService).getRegistrationStatistics();
        verify(graphService).getGeneralStatisticsForAllUsersByCities();
    }

}
