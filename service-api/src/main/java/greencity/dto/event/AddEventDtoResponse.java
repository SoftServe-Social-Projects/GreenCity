package greencity.dto.event;

import lombok.*;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotEmpty;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class AddEventDtoResponse {
    @NotEmpty
    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String description;

    @Max(7)
    private List<EventDateLocationDto> dates;

    @NotEmpty
    private EventAuthorDto organizer;
}
