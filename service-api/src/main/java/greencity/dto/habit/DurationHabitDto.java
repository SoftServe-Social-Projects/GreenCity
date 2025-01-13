package greencity.dto.habit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class DurationHabitDto {
    String description;
    Long duration;
}
