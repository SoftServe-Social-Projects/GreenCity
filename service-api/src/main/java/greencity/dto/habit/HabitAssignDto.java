package greencity.dto.habit;

import greencity.dto.user.UserToDoListItemAdvanceDto;
import greencity.enums.HabitAssignStatus;
import greencity.dto.habitstatuscalendar.HabitStatusCalendarDto;
import java.time.ZonedDateTime;
import java.util.List;
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
@Builder
@ToString
public class HabitAssignDto {
    private ZonedDateTime createDateTime;
    private Integer duration;
    private HabitDto habit;
    private List<UserToDoListItemAdvanceDto> userToDoListItems;
    private List<HabitStatusCalendarDto> habitStatusCalendarDtoList;
    private Integer habitStreak;
    private Long id;
    private ZonedDateTime lastEnrollmentDate;
    private HabitAssignStatus status;
    private Long userId;
    private Integer workingDays;
    private Boolean progressNotificationHasDisplayed;
    private List<Long> friendsIdsTrackingHabit;
}
