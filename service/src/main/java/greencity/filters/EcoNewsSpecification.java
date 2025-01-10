package greencity.filters;

import greencity.entity.EcoNews;
import greencity.entity.EcoNews_;
import greencity.entity.Tag_;
import greencity.entity.localization.TagTranslation_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.function.TriFunction;
import org.jetbrains.annotations.NotNull;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/**
 * Scope {@code prototype} is used for creation new bean
 * {@link EcoNewsSpecification} every time after new request.
 */
@Component
@Scope("prototype")
@Slf4j
@RequiredArgsConstructor
public class EcoNewsSpecification implements MySpecification<EcoNews> {
    private final transient List<SearchCriteria> searchCriteriaList;

    // Predicate Creators
    private final transient Map<String, TriFunction<Root<EcoNews>, CriteriaBuilder, SearchCriteria, Predicate>> pred =
        Map.of(
            "id", this::getNumericPredicate,
            "title", this::getStringPredicate,
            "text", this::getStringPredicate,
            "imagePath", this::getStringPredicate,
            "source", this::getStringPredicate,
            "author", this::getAuthorPredicate,
            "dateRange", this::getDataRangePredicate,
            "creationDate", this::getCreationDatePredicate,
            "tags", this::getTagsPredicate,
            "hidden", this::getBooleanPredicate);

    @Override
    public Predicate toPredicate(@NotNull Root<EcoNews> root, @NotNull CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
        Predicate allPredicates = criteriaBuilder.conjunction();
        for (SearchCriteria searchCriteria : searchCriteriaList) {
            TriFunction<Root<EcoNews>, CriteriaBuilder, SearchCriteria, Predicate> predicateCreator =
                pred.get(searchCriteria.getType());
            if (predicateCreator != null) {
                Predicate predicate = predicateCreator.apply(root, criteriaBuilder, searchCriteria);
                allPredicates = criteriaBuilder.and(allPredicates, predicate);
            }
        }
        criteriaQuery.orderBy(getOrderList(root, criteriaQuery, criteriaBuilder));
        return allPredicates;
    }

    private Predicate getTagsPredicate(Root<EcoNews> root, CriteriaBuilder criteriaBuilder,
        SearchCriteria searchCriteria) {
        if (searchCriteria.getValue().toString().trim().isEmpty()) {
            return criteriaBuilder.conjunction();
        }
        String[] tags = searchCriteria.getValue().toString().split(",");
        List<Predicate> tagPredicates = new ArrayList<>();
        for (String tag : tags) {
            tagPredicates.add(
                criteriaBuilder.like(
                    root.join(EcoNews_.tags).join(Tag_.tagTranslations).get(TagTranslation_.name)
                        .as(String.class),
                    "%" + tag.trim() + "%"));
        }
        return criteriaBuilder.and(tagPredicates.toArray(new Predicate[0]));
    }

    private Predicate getCreationDatePredicate(Root<EcoNews> root, CriteriaBuilder criteriaBuilder,
        SearchCriteria searchCriteria) {
        if (searchCriteria.getValue().toString().trim().isEmpty()) {
            return criteriaBuilder.conjunction();
        }
        try {
            String date = (String) searchCriteria.getValue();
            LocalDate localDate = LocalDate.parse(date);
            ZonedDateTime zdt1 = localDate.atStartOfDay(ZoneOffset.UTC);
            ZonedDateTime zdt2 = ZonedDateTime.of(LocalDateTime.of(localDate, LocalTime.MAX), ZoneOffset.UTC);
            return criteriaBuilder.between(root.get(searchCriteria.getKey()), zdt1, zdt2);
        } catch (DateTimeParseException ex) {
            return criteriaBuilder.disjunction();
        }
    }

    private List<Order> getOrderList(Root<EcoNews> root, CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
        List<Order> orderList = new ArrayList<>();
        for (Order order : criteriaQuery.getOrderList()) {
            String sortField = order.getExpression().toString();
            if (sortField.equals("likes")) {
                orderList
                    .add(order.isAscending() ? criteriaBuilder.asc(criteriaBuilder.size(root.get("usersLikedNews")))
                        : criteriaBuilder.desc(criteriaBuilder.size(root.get("usersLikedNews"))));
            } else if (sortField.equals("dislikes")) {
                orderList
                    .add(order.isAscending() ? criteriaBuilder.asc(criteriaBuilder.size(root.get("usersDislikedNews")))
                        : criteriaBuilder.desc(criteriaBuilder.size(root.get("usersDislikedNews"))));
            } else {
                orderList.add(order.isAscending() ? criteriaBuilder.asc(root.get(sortField))
                    : criteriaBuilder.desc(root.get(sortField)));
            }
        }
        return orderList;
    }
}
