package greencity.dto.habit;

import greencity.dto.todolistitem.CustomToDoListItemRequestDto;
import greencity.dto.todolistitem.ToDoListItemWithStatusRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToDoAndCustomToDoListsDto {
    @Valid
    List<@Valid ToDoListItemWithStatusRequestDto> toDoListItemDto;
    @Valid
    List<@Valid CustomToDoListItemRequestDto> customToDoListItemDto;
}
