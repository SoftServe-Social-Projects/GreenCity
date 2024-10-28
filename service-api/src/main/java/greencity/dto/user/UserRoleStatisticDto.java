package greencity.dto.user;

import greencity.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRoleStatisticDto {
    private Role role;
    private Long count;
}