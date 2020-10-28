package greencity.dto.user;

import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class BulkSaveUserGoalDto {
    @Valid List<@Valid UserGoalDto> userGoals;
    @Valid List<@Valid UserCustomGoalDto> userCustomGoal;
}
