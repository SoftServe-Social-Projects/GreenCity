package greencity.dto.user;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class UserManagementViewDto {
    private String id;
    private String name;
    private String email;
    private String userCredo;
    private String role;
    private String userStatus;
}
