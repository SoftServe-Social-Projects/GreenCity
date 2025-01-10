package greencity.rating;

import greencity.dto.ratingstatistics.RatingStatisticsVO;
import greencity.dto.user.UserVO;
import greencity.entity.RatingPoints;
import greencity.entity.RatingStatistics;
import greencity.entity.User;
import greencity.service.RatingStatisticsService;
import greencity.service.UserService;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RatingCalculation {
    private final RatingStatisticsService ratingStatisticsService;
    private final ModelMapper modelMapper;
    private final UserService userService;

    /**
     * Method that calculates the user rating.
     *
     * @param rating of {@link RatingPoints}
     * @param userVo of {@link UserVO}
     */
    public void ratingCalculation(RatingPoints rating, UserVO userVo) {
        User user = modelMapper.map(userVo, User.class);
        double newRating = userVo.getRating() + rating.getPoints();
        userVo.setRating(newRating > 0 ? newRating : 0);
        userService.updateUserRating(user.getId(), userVo.getRating());
        RatingStatistics ratingStatistics = RatingStatistics
            .builder()
            .rating(userVo.getRating())
            .ratingPoints(rating)
            .user(user)
            .pointsChanged(rating.getPoints())
            .build();
        ratingStatisticsService.save(modelMapper.map(ratingStatistics, RatingStatisticsVO.class));
    }
}
