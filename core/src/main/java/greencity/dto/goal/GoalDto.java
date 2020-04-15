package greencity.dto.goal;

import javax.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class GoalDto {
    @NotNull
    @Min(value = 1, message = "Goal id must be a positive number")
    private Long id;

    @NotEmpty(message = "Goal text must not be null")
    private String text;
}
