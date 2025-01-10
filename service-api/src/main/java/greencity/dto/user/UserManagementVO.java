package greencity.dto.user;

import greencity.enums.Role;
import greencity.enums.UserStatus;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class UserManagementVO {
    private Long id;
    private String name;
    private String email;
    private String userCredo;
    private Role role;
    private UserStatus userStatus;
}
