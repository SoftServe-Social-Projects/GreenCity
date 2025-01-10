package greencity.mapping;

import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievement.UserAchievementVO;
import greencity.dto.achievement.UserVOAchievement;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class UserVOAchievementMapper extends AbstractConverter<User, UserVOAchievement> {
    @Override
    protected UserVOAchievement convert(User user) {
        return UserVOAchievement.builder()
            .id(user.getId())
            .name(user.getName())
            .userAchievements(user.getUserAchievements() != null ? user.getUserAchievements()
                .stream().map(userAchievement -> UserAchievementVO.builder()
                    .id(userAchievement.getId())
                    .user(UserVO.builder()
                        .id(userAchievement.getUser().getId())
                        .build())
                    .achievement(AchievementVO.builder()
                        .id(userAchievement.getAchievement().getId())
                        .build())
                    .build())
                .collect(Collectors.toList()) : null)
            .build();
    }
}
