package greencity.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class UpdateEventDto {
    @NotNull
    private Long id;

    @Size(min = 1, max = 70)
    private String title;

    @Size(min = 20, max = 63206)
    private String description;

    @Size(max = 7)
    private List<EventDateLocationDto> datesLocations;

    private String titleImage;

    private List<String> additionalImages;

    private List<String> imagesToDelete;

    private List<String> tags;

    @JsonProperty(value = "open")
    private Boolean isOpen;
}
