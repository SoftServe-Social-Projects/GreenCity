package greencity.dto.todolistitem;

import java.util.List;
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
public class ToDoListItemManagementDto {
    @NotNull
    private Long id;

    private List<ToDoListItemTranslationVO> translations;
}
