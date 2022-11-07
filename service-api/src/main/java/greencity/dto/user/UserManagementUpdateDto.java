package greencity.dto.user;

import greencity.constant.ServiceValidationConstants;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.*;

@NoArgsConstructor
@Getter
@Setter
@Builder
@AllArgsConstructor
@EqualsAndHashCode
public class UserManagementUpdateDto {
    @NotBlank
    @Size(
        min = ServiceValidationConstants.USERNAME_MIN_LENGTH,
        max = ServiceValidationConstants.USERNAME_MAX_LENGTH)
    private String name;

    @Email(message = ServiceValidationConstants.INVALID_EMAIL)
    @NotBlank
    private String email;

    private String userCredo;

    @NotNull
    private Role role;

    @NotNull
    private UserStatus userStatus;
}
