package greencity.dto.todolistitem;

import greencity.dto.user.UserToDoListItemVO;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class CustomToDoListItemVO {
    private Long id;

    private String text;

    @Builder.Default
    private List<UserToDoListItemVO> userToDoListItems = new ArrayList<>();
}
