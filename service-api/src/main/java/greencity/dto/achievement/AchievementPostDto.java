package greencity.dto.achievement;

import greencity.dto.achievementcategory.AchievementCategoryDto;
import lombok.*;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class AchievementPostDto {
    private List<AchievementTranslationVO> translations;

    private AchievementCategoryDto achievementCategory;

    private Integer condition;
}
