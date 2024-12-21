package greencity.dto.event;

import jakarta.validation.constraints.Max;
import lombok.Builder;
import lombok.Data;
import org.springframework.lang.Nullable;
import java.util.List;

@Data
@Builder
public class EventResponseDto {
    private EventInformationDto eventInformation;

    @Max(7)
    private List<EventDateInformationDto> dates;

    @Nullable
    private String titleImage;

    @Nullable
    @Max(4)
    private List<String> additionalImages;
}
