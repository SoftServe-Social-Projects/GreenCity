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
import io.swagger.v3.oas.annotations.media.Schema;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class UpdateEventRequestDto {
    @NotNull
    @Schema(example = "1")
    private Long id;

    @NotEmpty
    @NotBlank
    @Size(min = 1, max = 70)
    @Schema(example = "string")
    private String title;

    @NotEmpty
    @NotBlank
    @Size(min = 20, max = 63206)
    @Schema(example = "string7string4string")
    private String description;

    @Size(min = 1, max = 7)
    @Schema(example = """
        [
        \t{
        \t\t"startDate":"2023-05-27T15:00:00Z",
        \t\t"finishDate":"2023-05-27T17:00:00Z",
        \t\t"coordinates":{
        \t\t\t"latitude":1,
        \t\t\t"longitude":1
        \t\t}
        \t}
        ]""")
    private List<UpdateEventDateLocationDto> datesLocations;

    @Schema(hidden = true)
    private String titleImage;

    @Schema(hidden = true)
    private List<String> additionalImages;

    @Schema(example = "[\"Social\"]")
    private List<String> tags;

    @JsonProperty(value = "open")
    @Schema(example = "true")
    private Boolean isOpen;
}
