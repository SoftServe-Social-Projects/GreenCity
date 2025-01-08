package greencity.filters;

import static org.mockito.Mockito.when;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import greencity.dto.ratingstatistics.RatingStatisticsViewDto;
import greencity.entity.RatingStatistics;
import greencity.entity.RatingStatistics_;
import greencity.entity.User;
import greencity.entity.User_;
import greencity.entity.RatingPoints;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import jakarta.persistence.criteria.*;
import jakarta.persistence.metamodel.SingularAttribute;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RatingStatisticsSpecificationTest {

    @Mock
    private CriteriaBuilder criteriaBuilderMock;

    @Mock
    private CriteriaQuery criteriaQueryMock;

    @Mock
    private Predicate predicateMock;

    @Mock
    private Path<Object> pathRatingStatisticsIdMock;

    @Mock
    private Path<Object> pathRatingStatisticsRatingMock;

    @Mock
    private Path<Object> pathRatingPointsNameMock;

    @Mock
    private Path<Long> pathUserIdMock;

    @Mock
    private Path<Object> pathCreateDate;

    @Mock
    private Root<RatingStatistics> ratingStatisticsRootMock;

    @Mock
    private Predicate andIdNumericPredicate;

    @Mock
    private Predicate andEventNamePredicate;

    @Mock
    private Predicate andDataRangePredicate;

    @Mock
    private Predicate andUserIdPredicate;

    @Mock
    private Predicate andCurrentRatingPredicate;

    @Mock
    private Join<RatingStatistics, User> userJoinMock;

    @Mock
    private Join<RatingStatistics, RatingPoints> ratingPointsJoinMock;

    @Mock
    private SingularAttribute<RatingStatistics, User> user;

    @Mock
    private SingularAttribute<User, Long> id;

    @Mock
    private SingularAttribute<User, String> email;

    private RatingStatisticsSpecification ratingStatisticsSpecification;

    private List<SearchCriteria> criteriaList;

    @BeforeEach
    void setUp() {
        RatingStatisticsViewDto ratingStatisticsViewDto =
            new RatingStatisticsViewDto("2", "UNDO_COMMENT_OR_REPLY", "1", "", "2021-01-12", "2021-01-13", "", "50");

        criteriaList = new ArrayList<>();
        criteriaList.add(
            SearchCriteria.builder()
                .key(RatingStatistics_.ID)
                .type(RatingStatistics_.ID)
                .value(ratingStatisticsViewDto.getId())
                .build());
        criteriaList.add(
            SearchCriteria.builder()
                .key(RatingStatistics_.RATING_POINTS)
                .type(RatingStatistics_.RATING_POINTS)
                .value(ratingStatisticsViewDto.getEventName())
                .build());
        criteriaList.add(
            SearchCriteria.builder()
                .key(RatingStatistics_.USER)
                .type("userId")
                .value(ratingStatisticsViewDto.getUserId())
                .build());
        criteriaList.add(
            SearchCriteria.builder()
                .key(RatingStatistics_.CREATE_DATE)
                .type("dateRange")
                .value(new String[] {ratingStatisticsViewDto.getStartDate(), ratingStatisticsViewDto.getEndDate()})
                .build());
        criteriaList.add(
            SearchCriteria.builder()
                .key(RatingStatistics_.RATING)
                .type("currentRating")
                .value(ratingStatisticsViewDto.getCurrentRating())
                .build());

        RatingStatistics_.user = user;
        User_.id = id;
        User_.email = email;
        ratingStatisticsSpecification = new RatingStatisticsSpecification(criteriaList);
    }

    @Test
    void toPredicate() {
        String eventNameLowerCase = criteriaList.get(1).getValue().toString().toLowerCase();
        Path<String> lowerPathMock = mock(Path.class);

        when(criteriaBuilderMock.conjunction()).thenReturn(predicateMock);

        when(ratingStatisticsRootMock.get("id")).thenReturn(pathRatingStatisticsIdMock);

        when(criteriaBuilderMock.equal(pathRatingStatisticsIdMock, criteriaList.get(0).getValue()))
            .thenReturn(andIdNumericPredicate);

        when(criteriaBuilderMock.and(predicateMock, andIdNumericPredicate)).thenReturn(andIdNumericPredicate);

        when(ratingStatisticsRootMock.join(RatingStatistics_.ratingPoints)).thenReturn(ratingPointsJoinMock);

        when(ratingPointsJoinMock.get("name")).thenReturn(pathRatingPointsNameMock);

        when(criteriaBuilderMock.lower(any())).thenReturn(lowerPathMock);

        when(criteriaBuilderMock.like(lowerPathMock, "%" + eventNameLowerCase + "%"))
            .thenReturn(andEventNamePredicate);

        when(criteriaBuilderMock.and(andIdNumericPredicate, andEventNamePredicate)).thenReturn(andEventNamePredicate);

        when(ratingStatisticsRootMock.join(RatingStatistics_.user)).thenReturn(userJoinMock);

        when(userJoinMock.get(User_.id)).thenReturn(pathUserIdMock);

        when(criteriaBuilderMock.equal(pathUserIdMock, "1")).thenReturn(andUserIdPredicate);

        when(criteriaBuilderMock.and(andEventNamePredicate, andUserIdPredicate)).thenReturn(andUserIdPredicate);

        when(ratingStatisticsRootMock.get(RatingStatistics_.CREATE_DATE)).thenReturn(pathCreateDate);

        when(criteriaBuilderMock.between(eq(ratingStatisticsRootMock.get(RatingStatistics_.CREATE_DATE)),
            any(ZonedDateTime.class), any(ZonedDateTime.class))).thenReturn(andDataRangePredicate);

        when(criteriaBuilderMock.and(andUserIdPredicate, andDataRangePredicate)).thenReturn(andDataRangePredicate);

        when(ratingStatisticsRootMock.get(RatingStatistics_.RATING)).thenReturn(pathRatingStatisticsRatingMock);

        when(criteriaBuilderMock.equal(pathRatingStatisticsRatingMock, criteriaList.get(4).getValue()))
            .thenReturn(andCurrentRatingPredicate);

        when(criteriaBuilderMock.and(andDataRangePredicate, andCurrentRatingPredicate))
            .thenReturn(andCurrentRatingPredicate);

        ratingStatisticsSpecification.toPredicate(ratingStatisticsRootMock, criteriaQueryMock, criteriaBuilderMock);

        verify(ratingStatisticsRootMock, never()).get(RatingStatistics_.RATING_POINTS);
        verify(ratingPointsJoinMock).get("name");
        verify(criteriaBuilderMock).like(lowerPathMock, "%" + eventNameLowerCase + "%");
        verify(criteriaBuilderMock).and(predicateMock, andIdNumericPredicate);
        verify(criteriaBuilderMock).and(andIdNumericPredicate, andEventNamePredicate);
        verify(criteriaBuilderMock).and(andEventNamePredicate, andUserIdPredicate);
        verify(criteriaBuilderMock).and(andUserIdPredicate, andDataRangePredicate);
        verify(criteriaBuilderMock).and(andDataRangePredicate, andCurrentRatingPredicate);
    }
}