package greencity.dto.ratingstatistics;

import greencity.enums.Status;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RatingPointsDto {
    private Long id;
    private String name;
    private Integer points;
    private Status status;
}
