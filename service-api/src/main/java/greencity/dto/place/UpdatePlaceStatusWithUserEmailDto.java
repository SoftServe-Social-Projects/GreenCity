package greencity.dto.place;

import greencity.enums.PlaceStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class UpdatePlaceStatusWithUserEmailDto {
    @NotNull
    private String placeName;

    @NotNull
    private PlaceStatus newStatus;

    @NotNull
    private String userName;

    @NotNull
    @Email
    private String email;
}
