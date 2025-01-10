package greencity.dto.user;

import jakarta.validation.constraints.NotNull;
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
@EqualsAndHashCode
@Builder
public class UserProfilePictureDto {
    @NotNull
    private Long id;

    @NotNull
    private String name;

    @NotNull
    private String profilePicturePath;
}
