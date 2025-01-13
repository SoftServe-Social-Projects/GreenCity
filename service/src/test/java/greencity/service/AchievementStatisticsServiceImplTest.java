package greencity.service;

import greencity.dto.achievement.StatisticsDto;
import greencity.repository.AchievementRepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.times;

class AchievementStatisticsServiceImplTest {
    @Mock
    private AchievementRepo achievementRepo;

    @InjectMocks
    private AchievementStatisticsServiceImpl achievementStatisticsService;

    private List<StatisticsDto> expectedStatistics = List.of(
        new StatisticsDto("Achievement 1", 10L),
        new StatisticsDto("Achievement 2", 20L));

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testStatisticsUsersWithAchievements() {
        when(achievementRepo.getStatisticsUsersWithAchievements()).thenReturn(expectedStatistics);

        List<StatisticsDto> actualStatistics = achievementStatisticsService.statisticsUsersWithAchievements();

        assertEquals(expectedStatistics, actualStatistics);
        verify(achievementRepo, times(1)).getStatisticsUsersWithAchievements();
    }

    @Test
    void testStatisticsUsersWithAchievementsCategory() {
        when(achievementRepo.getStatisticsUsersWithAchievementsCategory()).thenReturn(expectedStatistics);

        List<StatisticsDto> actualStatistics = achievementStatisticsService.statisticsUsersWithAchievementsCategory();

        assertEquals(expectedStatistics, actualStatistics);
        verify(achievementRepo, times(1)).getStatisticsUsersWithAchievementsCategory();
    }

    @Test
    void testStatisticsUsersActivity() {
        when(achievementRepo.getUserActivityStatistics()).thenReturn(expectedStatistics);

        List<StatisticsDto> actualStatistics = achievementStatisticsService.statisticsUsersActivity();

        assertEquals(expectedStatistics, actualStatistics);
        verify(achievementRepo, times(1)).getUserActivityStatistics();
    }
}