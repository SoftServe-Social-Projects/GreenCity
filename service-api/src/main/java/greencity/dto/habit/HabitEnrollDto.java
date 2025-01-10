package greencity.dto.habit;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@ToString
@Builder
public class HabitEnrollDto {
    private Long habitAssignId;
    private String habitName;
    private String habitDescription;
    private boolean isEnrolled;
}
