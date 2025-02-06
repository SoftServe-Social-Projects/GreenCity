package greencity.dto.event;

import jakarta.validation.constraints.NotNull;
import java.time.ZonedDateTime;

public record EventDateInformationDto(
    Long id,
    AddressDto coordinates,
    @NotNull(message = "Start date must not be null or empty") ZonedDateTime startDate,
    @NotNull(message = "Finish date must not be null or empty") ZonedDateTime finishDate,
    String onlineLink) {
}
