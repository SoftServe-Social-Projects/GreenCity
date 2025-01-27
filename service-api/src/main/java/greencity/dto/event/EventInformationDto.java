package greencity.dto.event;

import greencity.dto.tag.TagUaEnDto;
import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class EventInformationDto {
    private String title;
    private String description;
    @NotEmpty
    private List<TagUaEnDto> tags;
}
