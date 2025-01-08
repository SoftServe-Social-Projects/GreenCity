package greencity.dto.todolistitem;

import greencity.enums.ToDoListItemStatus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class CustomToDoListItemWithStatusSaveRequestDto extends CustomToDoListItemSaveRequestDto {
    ToDoListItemStatus status;
}
