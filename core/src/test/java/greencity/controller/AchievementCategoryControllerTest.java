package greencity.controller;

import greencity.service.AchievementCategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.Collections;

import static greencity.ModelUtils.getPrincipal;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AchievementCategoryControllerTest {

    private static final String achievementCategoryLink = "/achievements/categories";
    private MockMvc mockMvc;
    private final Principal principal = getPrincipal();

    @Mock
    private AchievementCategoryService achievementCategoryService;

    @InjectMocks
    private AchievementCategoryController achievementCategoryController;

    @BeforeEach
    void setup() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(achievementCategoryController)
            .build();
    }

    @Test
    void getAchievementCategoriesTest() throws Exception {
        when(achievementCategoryService.findAllWithAtLeastOneAchievement(anyString())).thenReturn(Collections.emptyList());
        mockMvc.perform(get(achievementCategoryLink).principal(principal))
                .andExpect(status().isOk());
        verify(achievementCategoryService).findAllWithAtLeastOneAchievement("test@gmail.com");
    }

}