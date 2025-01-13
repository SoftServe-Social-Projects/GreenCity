package greencity.dto.friends;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UserFriendHabitInviteDto extends UserFriendDto {
    private Boolean hasInvitation;
    private Boolean hasAcceptedInvitation;
}
