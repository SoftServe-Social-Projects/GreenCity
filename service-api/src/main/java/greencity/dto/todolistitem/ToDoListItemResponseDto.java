package greencity.dto.todolistitem;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.util.List;

@EqualsAndHashCode
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToDoListItemResponseDto {
    private Long id;

    private List<ToDoListItemTranslationDTO> translations;
}
