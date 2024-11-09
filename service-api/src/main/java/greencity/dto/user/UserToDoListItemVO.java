package greencity.dto.user;

import greencity.dto.todolistitem.ToDoListItemVO;
import greencity.dto.habit.HabitAssignVO;
import greencity.enums.ToDoListItemStatus;
import java.time.LocalDateTime;
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

    private ToDoListItemVO toDoListItemVO;

    @Builder.Default
    private ToDoListItemStatus status = ToDoListItemStatus.ACTIVE;

    private LocalDateTime dateCompleted;
}
