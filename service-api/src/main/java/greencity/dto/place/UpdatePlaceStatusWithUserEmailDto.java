package greencity.dto.place;

import greencity.enums.PlaceStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class UpdatePlaceStatusWithUserEmailDto {
    @NotBlank
    private String placeName;

    @NotNull
    private PlaceStatus newStatus;

    @NotBlank
    private String userName;

    @Email
    private String email;
}
