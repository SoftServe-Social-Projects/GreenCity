package greencity.dto.ratingstatistics;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import lombok.ToString;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class RatingStatisticsViewDto {
    private String id;
    private String eventName;
    private String userId;
    private String userEmail;
    private String startDate;
    private String endDate;
    private String pointsChanged;
    private String currentRating;
}
