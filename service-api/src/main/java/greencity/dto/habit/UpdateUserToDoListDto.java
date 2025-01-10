package greencity.dto.habit;

import greencity.dto.user.UserToDoListItemAdvanceDto;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Getter
@Setter
@Builder
public class UpdateUserToDoListDto {
    private Long habitAssignId;
    private Long userToDoListItemId;
    private List<UserToDoListItemAdvanceDto> userToDoListAdvanceDto;
}
