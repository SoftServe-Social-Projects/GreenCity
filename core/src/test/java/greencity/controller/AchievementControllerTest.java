package greencity.controller;

import greencity.dto.achievement.ActionDto;
import greencity.service.AchievementService;
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
import java.security.Principal;
import static greencity.ModelUtils.getActionDto;
import static greencity.ModelUtils.getPrincipal;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AchievementControllerTest {
    private static final String achievementLink = "/achievements";
    private MockMvc mockMvc;
    private final Principal principal = getPrincipal();

    @InjectMocks
    private AchievementController achievementController;

    @Mock
    private AchievementService achievementService;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(achievementController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void findAllTest() throws Exception {
        mockMvc.perform(get(achievementLink).principal(principal)).andExpect(status().isOk());
        verify(achievementService).findAllByType("test@gmail.com", null);
    }

    @Test
    void findAllAchievedTest() throws Exception {
        mockMvc.perform(get(achievementLink).principal(principal).param("achievementStatus", "ACHIEVED"))
            .andExpect(status().isOk());
        verify(achievementService).findAllByType("test@gmail.com", "ACHIEVED");
    }

    @Test
    void findAllUnAchievedTest() throws Exception {
        mockMvc.perform(get(achievementLink).principal(principal).param("achievementStatus", "UNACHIEVED"))
            .andExpect(status().isOk());
        verify(achievementService).findAllByType("test@gmail.com", "UNACHIEVED");
    }

    @Test
    void findAllAchievedIgnoreCaseTest() throws Exception {
        mockMvc.perform(get(achievementLink).principal(principal).param("achievementStatus", "AchieVED"))
            .andExpect(status().isOk());
        verify(achievementService).findAllByType("test@gmail.com", "AchieVED");
    }

    @Test
    void findAllUnAchievedIgnoreCaseTest() throws Exception {
        mockMvc.perform(get(achievementLink).principal(principal).param("achievementStatus", "unAchieVED"))
            .andExpect(status().isOk());
        verify(achievementService).findAllByType("test@gmail.com", "unAchieVED");
    }

    @Test
    void achieveTest() {
        var dto = getActionDto();
        achievementController.achieve(ActionDto.builder().build());
        verify(achievementService).achieve(dto);
    }
}
