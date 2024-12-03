package greencity.dto.event;

import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@Data
public abstract class AbstractEventDateLocationDto {
    @NotNull(message = "Start date must not be null or empty")
    private ZonedDateTime startDate;

    @NotNull(message = "Finish date must not be null or empty")
    private ZonedDateTime finishDate;

    private String onlineLink;

    public abstract <T extends UpdateAddressDto> T getCoordinates();
}
