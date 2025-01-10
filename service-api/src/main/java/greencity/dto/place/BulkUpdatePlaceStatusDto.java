package greencity.dto.place;

import greencity.constant.ServiceValidationConstants;
import greencity.enums.PlaceStatus;
import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class BulkUpdatePlaceStatusDto {
    @Valid
    private List<@Positive(message = ServiceValidationConstants.NEGATIVE_ID) @NotNull Long> ids;

    @NotNull
    private PlaceStatus status;
}
