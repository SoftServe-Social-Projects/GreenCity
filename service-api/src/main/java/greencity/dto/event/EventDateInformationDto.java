package greencity.dto.event;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
public class EventDateInformationDto extends AbstractEventDateLocationDto {
    private Long id;
    private EventResponseDto event;
    private AddressDto coordinates;
}
