package greencity.dto.habit;

import greencity.dto.user.UserVO;
import java.time.ZonedDateTime;
import greencity.enums.HabitAssignStatus;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class HabitAssignVO {
    private Long id;
    private HabitAssignStatus status;
    private ZonedDateTime createDateTime;
    private HabitVO habitVO;
    private UserVO userVO;
    private Integer duration;
    private Integer habitStreak;
    private Integer workingDays;
    private ZonedDateTime lastEnrollmentDate;
}
