package greencity.filters;

import greencity.entity.RatingStatistics;
import greencity.entity.RatingStatistics_;
import greencity.entity.User;
import greencity.entity.User_;
import greencity.entity.RatingPoints;
import jakarta.persistence.criteria.*;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class RatingStatisticsSpecification implements MySpecification<RatingStatistics> {
    private final transient List<SearchCriteria> searchCriteriaList;

    @Override
    public Predicate toPredicate(Root<RatingStatistics> root, CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
        Predicate allPredicates = criteriaBuilder.conjunction();
        for (SearchCriteria searchCriteria : searchCriteriaList) {
            if (searchCriteria.getType().equals("id")) {
                allPredicates =
                    criteriaBuilder.and(allPredicates, getNumericPredicate(root, criteriaBuilder, searchCriteria));
            }
            if (searchCriteria.getType().equals("ratingPoints")) {
                allPredicates =
                    criteriaBuilder.and(allPredicates, getEventNamePredicate(root, criteriaBuilder, searchCriteria));
            }
            if (searchCriteria.getType().equals("userId")) {
                allPredicates =
                    criteriaBuilder.and(allPredicates, getUserIdPredicate(root, criteriaBuilder, searchCriteria));
            }
            if (searchCriteria.getType().equals("userMail")) {
                allPredicates =
                    criteriaBuilder.and(allPredicates, getUserMailPredicate(root, criteriaBuilder, searchCriteria));
            }
            if (searchCriteria.getType().equals("dateRange")) {
                allPredicates =
                    criteriaBuilder.and(allPredicates, getDataRangePredicate(root, criteriaBuilder, searchCriteria));
            }
            if (searchCriteria.getType().equals("pointsChanged")) {
                allPredicates =
                    criteriaBuilder.and(allPredicates, getNumericPredicate(root, criteriaBuilder, searchCriteria));
            }
            if (searchCriteria.getType().equals("currentRating")) {
                allPredicates =
                    criteriaBuilder.and(allPredicates, getNumericPredicate(root, criteriaBuilder, searchCriteria));
            }
        }
        return allPredicates;
    }

    private Predicate getEventNamePredicate(Root<RatingStatistics> root, CriteriaBuilder criteriaBuilder,
        SearchCriteria searchCriteria) {
        Join<RatingStatistics, RatingPoints> ratingPointsJoin = root.join(RatingStatistics_.ratingPoints);
        return criteriaBuilder.like(criteriaBuilder.lower(ratingPointsJoin.get("name")),
            "%" + searchCriteria.getValue().toString().toLowerCase() + "%");
    }

    private Predicate getUserMailPredicate(Root<RatingStatistics> root, CriteriaBuilder criteriaBuilder,
        SearchCriteria searchCriteria) {
        Join<RatingStatistics, User> userJoin = root.join(RatingStatistics_.user);
        return criteriaBuilder.like(userJoin.get(User_.email), "%" + searchCriteria.getValue() + "%");
    }

    private Predicate getUserIdPredicate(Root<RatingStatistics> root, CriteriaBuilder criteriaBuilder,
        SearchCriteria searchCriteria) {
        Join<RatingStatistics, User> userJoin = root.join(RatingStatistics_.user);

        try {
            return criteriaBuilder.equal(userJoin.get(User_.id), searchCriteria.getValue());
        } catch (NumberFormatException ex) {
            return searchCriteria.getValue().toString().trim().isEmpty() ? criteriaBuilder.conjunction()
                : criteriaBuilder.disjunction();
        }
    }
}
