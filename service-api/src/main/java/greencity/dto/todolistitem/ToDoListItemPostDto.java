package greencity.dto.todolistitem;

import greencity.annotations.LanguageTranslationConstraint;
import greencity.dto.language.LanguageTranslationDTO;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class ToDoListItemPostDto {
    @Valid
    @LanguageTranslationConstraint
    private List<LanguageTranslationDTO> translations;

    @Valid
    @NotNull
    private ToDoListItemRequestDto toDoListItem;
}
