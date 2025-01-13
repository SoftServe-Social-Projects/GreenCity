package greencity.dto.todolistitem;

import greencity.dto.language.LanguageVO;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToDoListItemTranslationDTO {
    private Long id;

    private LanguageVO language;

    private String content;
}
