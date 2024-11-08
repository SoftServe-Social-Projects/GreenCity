package greencity.filters;

import greencity.entity.ToDoListItem;
import greencity.entity.ToDoListItem_;
import greencity.entity.Translation_;
import greencity.entity.localization.ToDoListItemTranslation;
import greencity.entity.localization.ToDoListItemTranslation_;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.List;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ToDoListItemSpecification implements MySpecification<ToDoListItem> {
    private final transient List<SearchCriteria> searchCriteriaList;

    @Override
    public Predicate toPredicate(Root<ToDoListItem> root, CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder) {
        Predicate allPredicates = criteriaBuilder.conjunction();
        for (SearchCriteria searchCriteria : searchCriteriaList) {
            if (searchCriteria.getType().equals("id")) {
                allPredicates =
                    criteriaBuilder.and(allPredicates, getNumericPredicate(root, criteriaBuilder, searchCriteria));
            }
            if (searchCriteria.getType().equals("content")) {
                allPredicates = criteriaBuilder.and(allPredicates,
                    getTranslationPredicate(root, criteriaQuery, criteriaBuilder, searchCriteria));
            }
        }
        return allPredicates;
    }

    private Predicate getTranslationPredicate(Root<ToDoListItem> root, CriteriaQuery<?> criteriaQuery,
        CriteriaBuilder criteriaBuilder, SearchCriteria searchCriteria) {
        Root<ToDoListItemTranslation> itemTranslationroot = criteriaQuery.from(ToDoListItemTranslation.class);
        return searchCriteria.getValue().toString().trim().isEmpty() ? criteriaBuilder.conjunction()
            : criteriaBuilder.and(criteriaBuilder.like(itemTranslationroot.get(Translation_.content),
                "%" + searchCriteria.getValue() + "%"),
                criteriaBuilder.equal(
                    itemTranslationroot.get(ToDoListItemTranslation_.toDoListItem).get(ToDoListItem_.id),
                    root.get(ToDoListItem_.id)));
    }
}
