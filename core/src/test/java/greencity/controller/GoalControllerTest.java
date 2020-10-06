package greencity.controller;

import greencity.service.GoalService;
import java.util.Locale;
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

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GoalControllerTest {
    private static final String goalLink = "/goals";
    private MockMvc mockMvc;
    @InjectMocks
    private GoalController goalController;
    @Mock
    private GoalService goalService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders
            .standaloneSetup(goalController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void getAllTest() throws Exception {
        mockMvc.perform(get(goalLink).locale(new Locale("ru")))
            .andExpect(status().isOk());
        verify(goalService).findAll(eq("ru"));
    }

    @Test
    void getShoppingList() throws Exception {
        mockMvc.perform(get(goalLink + "/shoppingList/{userId}", 1)
            .locale(new Locale("ru")))
            .andExpect(status().isOk());
        verify(goalService).getShoppingList(eq(1L), eq("ru"));
    }

    @Test
    void updateUserProfilePicture() throws Exception {
        mockMvc.perform(patch(goalLink + "/shoppingList/{userId}?status=false&goalId=1", 1))
            .andExpect(status().isOk());
        verify(goalService).changeGoalOrCustomGoalStatus(eq(1L), eq(false), eq(1L), eq(null));
    }
}
