package greencity.dto.user;

import greencity.dto.habit.HabitAssignVO;
import java.time.LocalDateTime;
import greencity.enums.UserToDoListItemStatus;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class UserToDoListItemVO {
    private Long id;

    private HabitAssignVO habitAssign;

    private Long targetId;

    private Boolean isCustomItem;

    @Builder.Default
    private UserToDoListItemStatus status = UserToDoListItemStatus.INPROGRESS;

    private LocalDateTime dateCompleted;
}
