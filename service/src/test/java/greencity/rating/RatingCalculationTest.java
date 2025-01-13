package greencity.rating;

import greencity.ModelUtils;
import greencity.dto.ratingstatistics.RatingPointsDto;
import greencity.entity.RatingPoints;
import greencity.repository.RatingPointsRepo;
import greencity.dto.ratingstatistics.RatingStatisticsVO;
import greencity.dto.user.UserVO;
import greencity.entity.RatingStatistics;
import greencity.entity.User;
import greencity.service.RatingStatisticsService;
import java.time.ZonedDateTime;

import greencity.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RatingCalculationTest {
    @Mock
    private RatingStatisticsService ratingStatisticsService;
    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private RatingCalculation ratingCalculation;
    @Mock
    private UserService userService;
    @Mock
    private RatingPointsRepo ratingPointsRepo;

    @Test
    void ratingCalculation() {
        RatingPoints rating = RatingPoints.builder()
            .id(1L)
            .name("COMMENT_OR_REPLY")
            .points(5)
            .build();
        User user = ModelUtils.getUser();
        user.setRating(1D);
        UserVO userVO = ModelUtils.getUserVO();
        userVO.setRating(1D);
        ZonedDateTime now = ZonedDateTime.now();
        RatingStatistics ratingStatistics = RatingStatistics
            .builder()
            .rating(userVO.getRating() + rating.getPoints())
            .ratingPoints(rating)
            .user(user)
            .pointsChanged(rating.getPoints())
            .build();

        RatingStatisticsVO ratingStatisticsVO = RatingStatisticsVO.builder()
            .id(1L)
            .rating(userVO.getRating())
            .ratingPoints(RatingPointsDto.builder()
                .id(rating.getId())
                .name(rating.getName())
                .points(rating.getPoints())
                .build())
            .user(userVO)
            .createDate(now)
            .pointsChanged(rating.getPoints())
            .build();
        when(modelMapper.map(userVO, User.class)).thenReturn(user);
        doNothing().when(userService).updateUserRating(1L, 6.0d);
        when(modelMapper.map(ratingStatistics, RatingStatisticsVO.class)).thenReturn(ratingStatisticsVO);
        when(ratingStatisticsService.save(ratingStatisticsVO)).thenReturn(ratingStatisticsVO);

        ratingCalculation.ratingCalculation(rating, userVO);

        verify(modelMapper).map(userVO, User.class);
        verify(userService).updateUserRating(1L, 6.0d);
        verify(modelMapper).map(ratingStatistics, RatingStatisticsVO.class);
        verify(ratingStatisticsService).save(ratingStatisticsVO);
    }
}