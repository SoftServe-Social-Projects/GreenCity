package greencity.dto.todolistitem;

import greencity.enums.ToDoListItemStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class ToDoListItemResponseWithStatusDto {
    @NotNull
    @Min(value = 1)
    private Long id;

    @NotEmpty
    private String text;

    private ToDoListItemStatus status;
}