package greencity.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class UpdateEventDto {
    @NotNull
    private Long id;

    @NotEmpty
    @NotBlank
    @Size(min = 1, max = 70)
    private String title;

    @Size(min = 20, max = 63206)
    private String description;

    @Size(min = 1, max = 7)
    private List<EventDateLocationDto> datesLocations;

    private String titleImage;

    private List<String> additionalImages;

    private List<String> tags;

    @JsonProperty(value = "open")
    private Boolean isOpen;
}
