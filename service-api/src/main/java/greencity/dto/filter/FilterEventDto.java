package greencity.dto.filter;

import greencity.enums.EventStatus;
import greencity.enums.EventTime;
import greencity.enums.EventType;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FilterEventDto {
    private EventTime time;
    private List<String> cities;
    private List<EventStatus> statuses;
    private List<String> tags;
    private String title;
    private EventType type;
}
