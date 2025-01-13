package greencity.dto.todolistitem;

import greencity.dto.user.UserToDoListItemVO;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.ToString;
import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class ToDoListItemVO {
    private Long id;

    private List<UserToDoListItemVO> userToDoListItemsVO;

    private List<ToDoListItemTranslationVO> translations;
}
