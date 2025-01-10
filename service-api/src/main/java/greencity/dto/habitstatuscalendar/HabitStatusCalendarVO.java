package greencity.dto.habitstatuscalendar;

import greencity.dto.habit.HabitAssignVO;
import java.time.LocalDate;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class HabitStatusCalendarVO {
    private Long id;
    private LocalDate enrollDate;
    private HabitAssignVO habitAssignVO;
}
