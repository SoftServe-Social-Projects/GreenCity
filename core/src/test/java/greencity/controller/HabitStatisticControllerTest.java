package greencity.controller;

import com.google.gson.Gson;
import greencity.dto.habitstatistic.AddHabitStatisticDto;
import greencity.dto.habitstatistic.UpdateHabitStatisticDto;
import static greencity.enums.HabitRate.GOOD;
import greencity.service.HabitStatisticService;
import java.time.ZonedDateTime;
import java.util.Locale;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class HabitStatisticControllerTest {

    private MockMvc mockMvc;

    @Mock
    HabitStatisticService habitStatisticService;

    @InjectMocks
    HabitStatisticController habitStatisticController;

    private static final String habitLink = "/habit/statistic";

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(habitStatisticController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void save() throws Exception {
        String json = """
            {
              "amountOfItems": 1,
              "createDate": "2020-10-09T16:49:01.020Z",
              "habitRate": "GOOD"
            }
            """;

        AddHabitStatisticDto addHabitStatisticDto = new AddHabitStatisticDto();
        addHabitStatisticDto.setAmountOfItems(1);
        addHabitStatisticDto.setCreateDate(ZonedDateTime.parse("2020-10-09T16:49:01.020Z"));
        addHabitStatisticDto.setHabitRate(GOOD);
        mockMvc.perform(post(habitLink + "/{habitId}", 1L)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());
        verify(habitStatisticService).saveByHabitIdAndUserId(1L, null, addHabitStatisticDto);
    }

    @Test
    void updateStatistic() throws Exception {
        UpdateHabitStatisticDto habitStatisticForUpdateDto = new UpdateHabitStatisticDto();
        habitStatisticForUpdateDto.setAmountOfItems(1);
        habitStatisticForUpdateDto.setHabitRate(GOOD);
        Gson gson = new Gson();
        String json = gson.toJson(habitStatisticForUpdateDto);
        mockMvc.perform(put(habitLink + "/{id}", 1)
            .content(json)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
        verify(habitStatisticService).update(1L, 1L, habitStatisticForUpdateDto);
    }

    @Test
    void findAllByHabitId() throws Exception {
        mockMvc.perform(get(habitLink + "/{habitId}", 1))
            .andExpect(status().isOk());
        verify(habitStatisticService).findAllStatsByHabitId(1L);
    }

    @Test
    void getTodayStatisticsForAllHabitItems() throws Exception {
        Locale locale = Locale.of("en");
        Gson gson = new Gson();
        String json = gson.toJson(locale);
        mockMvc.perform(get(habitLink + "/todayStatisticsForAllHabitItems")
            .content(json))
            .andExpect(status().isOk());
        verify(habitStatisticService).getTodayStatisticsForAllHabitItems(locale.getLanguage());
    }

    @Test
    void findAllStatsByHabitAssignId() throws Exception {
        mockMvc.perform(get(habitLink + "/assign/{habitAssignId}", 1L))
            .andExpect(status().isOk());
        verify(habitStatisticService).findAllStatsByHabitAssignId(1L);
    }

    @Test
    void findAmountOfAcquiredHabits() throws Exception {
        mockMvc.perform(get(habitLink + "/acquired/count")
            .param("userId", "1"))
            .andExpect(status().isOk());
        verify(habitStatisticService).getAmountOfAcquiredHabitsByUserId(1L);
    }

    @Test
    void findAmountOfHabitsInProgress() throws Exception {
        mockMvc.perform(get(habitLink + "/in-progress/count")
            .param("userId", "1"))
            .andExpect(status().isOk());
        verify(habitStatisticService).getAmountOfHabitsInProgressByUserId(1L);
    }
}
