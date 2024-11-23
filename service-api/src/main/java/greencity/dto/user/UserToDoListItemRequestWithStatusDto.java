package greencity.dto.user;

import greencity.enums.UserToDoListItemStatus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@SuperBuilder
public class UserToDoListItemRequestWithStatusDto extends UserToDoListItemRequestDto {
    private UserToDoListItemStatus status;
}
