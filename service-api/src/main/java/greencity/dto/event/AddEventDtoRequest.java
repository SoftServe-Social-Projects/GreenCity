package greencity.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import greencity.annotations.DecodedSize;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class AddEventDtoRequest {
    @NotBlank
    @DecodedSize(min = 1, max = 70)
    private String title;

    @NotBlank
    @Size(min = 20, max = 63206)
    private String description;

    @NotEmpty
    @Valid
    private List<EventDateLocationDto> datesLocations;

    @NotEmpty
    private List<String> tags;

    @JsonProperty(value = "open")
    private boolean isOpen;
}
