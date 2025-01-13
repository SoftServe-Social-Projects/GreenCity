package greencity.dto.todolistitem;

import greencity.enums.ToDoListItemStatus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import jakarta.validation.constraints.NotNull;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class ToDoListItemWithStatusRequestDto extends ToDoListItemRequestDto {
    @NotNull
    private ToDoListItemStatus status;
}
