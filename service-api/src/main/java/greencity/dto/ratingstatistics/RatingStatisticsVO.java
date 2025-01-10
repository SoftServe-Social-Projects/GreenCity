package greencity.dto.ratingstatistics;

import greencity.dto.user.UserVO;
import java.time.ZonedDateTime;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RatingStatisticsVO {
    private Long id;
    private ZonedDateTime createDate;
    private RatingPointsDto ratingPoints;
    private double pointsChanged;
    private double rating;
    private UserVO user;
}
