package greencity.dto.event;

import greencity.dto.tag.TagUaEnDto;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import java.util.List;

public record EventInformationDto(
    @Size(min = 1, max = 255) String title,
    @Size(max = 5000) String description,
    @NotEmpty List<TagUaEnDto> tags) {
}
