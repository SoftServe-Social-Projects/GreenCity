package greencity.dto.user;

import greencity.constant.ServiceValidationConstants;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class UserForListDto {
    @NotNull
    private Long id;

    @NotBlank
    @Size(
        min = ServiceValidationConstants.USERNAME_MIN_LENGTH,
        max = ServiceValidationConstants.USERNAME_MAX_LENGTH)
    private String name;

    private LocalDateTime dateOfRegistration;

    @Email(message = ServiceValidationConstants.INVALID_EMAIL)
    @NotBlank
    private String email;

    @NotNull
    private UserStatus userStatus;

    @NotNull
    private Role role;

    private String userCredo;
}