package greencity.dto.habit;

import greencity.enums.HabitAssignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class HabitAssignPreviewDto {
    private Integer duration;
    private HabitPreviewDto habit;
    private Long id;
    private HabitAssignStatus status;
    private Long userId;
    private Integer workingDays;
}
