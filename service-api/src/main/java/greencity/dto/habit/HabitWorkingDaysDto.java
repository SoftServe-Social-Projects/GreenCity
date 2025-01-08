package greencity.dto.habit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class HabitWorkingDaysDto {
    private Long userId;
    private Integer duration;
    private Integer workingDays;
}
