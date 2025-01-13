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
public class EventDateLocationDto extends AbstractEventDateLocationDto {
    private Long id;
    private EventDto event;
    private AddressDto coordinates;
}
