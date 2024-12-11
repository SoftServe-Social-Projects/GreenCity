package greencity.dto.place;

import greencity.enums.PlaceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;


@NoArgsConstructor
@AllArgsConstructor
@Getter
public class UpdatePlaceStatusWithUserEmailDto {
    @NotNull
    private PlaceStatus newStatus;

    @NotNull
    private String userEmail;

    @NotNull
    private String placeName;

    @NotNull
    private String userName;
}
