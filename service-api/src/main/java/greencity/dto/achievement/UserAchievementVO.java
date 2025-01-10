package greencity.dto.achievement;

import greencity.dto.user.UserVO;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Data;
import lombok.Builder;
import jakarta.validation.constraints.NotEmpty;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class UserAchievementVO {
    @NotEmpty
    private Long id;

    @NotEmpty
    private UserVO user;

    @NotEmpty
    private AchievementVO achievement;

    @NotEmpty
    private boolean notified;
}
