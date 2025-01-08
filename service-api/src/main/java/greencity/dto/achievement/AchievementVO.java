package greencity.dto.achievement;

import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.habit.HabitVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AchievementVO {
    private Long id;
    private String title;

    private String name;
    private String nameEng;
    @NotEmpty
    private AchievementCategoryVO achievementCategory;

    private HabitVO habit;

    @NotEmpty
    private Integer condition;

    private Integer progress;
}
