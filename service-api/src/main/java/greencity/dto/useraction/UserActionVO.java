package greencity.dto.useraction;

import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.user.UserVO;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@EqualsAndHashCode
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserActionVO {
    private Long id;

    private UserVO user;

    private AchievementCategoryVO achievementCategory;

    @Builder.Default
    private Integer count = 0;
}
