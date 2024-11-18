package greencity.dto.habit;

import greencity.dto.todolistitem.CustomToDoListItemRequestDto;
import greencity.dto.todolistitem.ToDoListItemDto;
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
    List<@Valid ToDoListItemDto> toDoListItemDto;
    @Valid
    List<@Valid CustomToDoListItemRequestDto> customToDoListItemDto;
}
