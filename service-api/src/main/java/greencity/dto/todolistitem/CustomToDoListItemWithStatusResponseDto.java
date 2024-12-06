package greencity.dto.todolistitem;

import greencity.enums.ToDoListItemStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CustomToDoListItemWithStatusResponseDto {
    @Min(1)
    private Long id;
    @NotEmpty
    private String text;
    @NotNull
    private ToDoListItemStatus status;
}
