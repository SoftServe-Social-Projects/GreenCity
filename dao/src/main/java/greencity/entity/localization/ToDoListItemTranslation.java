package greencity.entity.localization;

import greencity.entity.ToDoListItem;
import greencity.entity.Translation;
import lombok.*;
import lombok.experimental.SuperBuilder;
import jakarta.persistence.Entity;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "to_do_list_item_translations")
@EqualsAndHashCode(callSuper = true, exclude = "toDoListItem")
@SuperBuilder
@NoArgsConstructor
public class ToDoListItemTranslation extends Translation {
    @Getter
    @Setter
    @ManyToOne
    private ToDoListItem toDoListItem;
}
