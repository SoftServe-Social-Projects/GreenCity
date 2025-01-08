package greencity.dto.user;

import greencity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserStatusStatisticDto {
    private UserStatus status;
    private Long count;
}