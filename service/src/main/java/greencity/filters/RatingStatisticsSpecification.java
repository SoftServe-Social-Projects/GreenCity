package greencity.filters;

import greencity.annotations.RatingCalculationEnum;
import greencity.entity.RatingStatistics;
import greencity.entity.RatingStatistics_;
import greencity.entity.User;
import greencity.entity.User_;
import lombok.NoArgsConstructor;
import javax.persistence.criteria.*;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor
public class RatingStatisticsSpecification implements MySpecification<RatingStatistics> {
    private transient List<SearchCriteria> searchCriteriaList;

    /**
     * Constructor.
     */
    public RatingStatisticsSpecification(List<SearchCriteria> searchCriteriaList) {
        this.searchCriteriaList = searchCriteriaList;
    }

    @Override
    public Predicate toPredicate(Root<RatingStatistics> root, CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
        Predicate allPredicates = criteriaBuilder.conjunction();
        for (SearchCriteria searchCriteria : searchCriteriaList) {
            if (searchCriteria.getType().equals("id")) {
                allPredicates =
                    criteriaBuilder.and(allPredicates, getNumericPredicate(root, criteriaBuilder, searchCriteria));
            }
            if (searchCriteria.getType().equals("enum")) {
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
        List<RatingCalculationEnum> enumValues = Arrays.asList(RatingCalculationEnum.values());
        List<RatingCalculationEnum> selectedEnums = enumValues.stream()
            .filter(x -> x.toString().toLowerCase().contains(((String) searchCriteria.getValue()).toLowerCase()))
            .collect(Collectors.toList());

        Predicate predicate = criteriaBuilder.disjunction();
        for (RatingCalculationEnum ratingCalculationEnum : selectedEnums) {
            predicate = criteriaBuilder.or(predicate,
                criteriaBuilder.equal(root.get(searchCriteria.getKey()), ratingCalculationEnum));
        }
        return predicate;
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
            return searchCriteria.getValue().toString().trim().equals("") ? criteriaBuilder.conjunction()
                : criteriaBuilder.disjunction();
        }
    }
}
