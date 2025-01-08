package greencity.dto.ratingstatistics;

import greencity.dto.user.UserVO;
import java.time.ZonedDateTime;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RatingStatisticsDto {
    private Long id;
    private ZonedDateTime createDate;
    private RatingPointsDto ratingPoints;
    private float pointsChanged;
    private float rating;
    private UserVO user;
}
