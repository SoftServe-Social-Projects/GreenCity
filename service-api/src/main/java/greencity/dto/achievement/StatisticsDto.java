package greencity.dto.achievement;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class StatisticsDto {
    private String name;
    private Long count;
}
