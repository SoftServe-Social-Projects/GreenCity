package greencity.dto.goal;

import javax.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import static greencity.constant.ValidationConstants.CUSTOM_GOAL_TEXT_CANNOT_BE_EMPTY;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CustomGoalSaveRequestDto {
    @NotBlank(message = CUSTOM_GOAL_TEXT_CANNOT_BE_EMPTY)
    private String text;
}
