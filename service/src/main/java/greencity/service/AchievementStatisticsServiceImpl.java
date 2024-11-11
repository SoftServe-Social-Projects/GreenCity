package greencity.service;

import greencity.dto.achievement.StatisticsDto;
import greencity.repository.AchievementRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AchievementStatisticsServiceImpl implements AchievementStatisticsService {
    private final AchievementRepo achievementRepo;

    @Override
    public List<StatisticsDto> statisticsUsersWithAchievements() {
        return achievementRepo.getStatisticsUsersWithAchievements();
    }

    @Override
    public List<StatisticsDto> statisticsUsersWithAchievementsCategory() {
        return achievementRepo.getStatisticsUsersWithAchievementsCategory();
    }

    @Override
    public List<StatisticsDto> statisticsUsersActivity() {
        return achievementRepo.getUserActivityStatistics();
    }
}
