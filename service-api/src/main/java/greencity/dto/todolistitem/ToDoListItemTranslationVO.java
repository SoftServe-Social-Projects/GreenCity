package greencity.dto.todolistitem;

import greencity.dto.translation.TranslationVO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true, exclude = "toDoListItemVO")
@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
public class ToDoListItemTranslationVO extends TranslationVO {
    private ToDoListItemVO toDoListItemVO;
}
