package greencity.dto.place;

import greencity.constant.ServiceValidationConstants;
import greencity.enums.PlaceStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class UpdatePlaceStatusDto {
    @Positive(message = ServiceValidationConstants.NEGATIVE_ID)
    @NotNull
    private Long id;

    @NotNull
    private PlaceStatus status;
}
