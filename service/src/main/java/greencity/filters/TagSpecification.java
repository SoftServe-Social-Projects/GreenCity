package greencity.filters;

import greencity.entity.Tag;
import greencity.entity.Tag_;
import greencity.entity.localization.TagTranslation;
import greencity.entity.localization.TagTranslation_;
import lombok.AllArgsConstructor;
import jakarta.persistence.criteria.*;
import java.util.List;

@AllArgsConstructor
public class TagSpecification implements MySpecification<Tag> {
    private transient List<SearchCriteria> searchCriteriaList;

    @Override
    public Predicate toPredicate(Root<Tag> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        Predicate allPredicates = criteriaBuilder.conjunction();
        criteriaQuery.distinct(true);

        for (SearchCriteria searchCriteria : searchCriteriaList) {
            if (searchCriteria.getType().equals("id")) {
                allPredicates = criteriaBuilder.and(allPredicates,
                    getNumericPredicate(root, criteriaBuilder, searchCriteria));
            }

            if (searchCriteria.getType().equals("type")) {
                allPredicates = criteriaBuilder.and(allPredicates,
                    getEnumPredicate(root, criteriaBuilder, searchCriteria));
            }

            if (searchCriteria.getType().equals("name")) {
                allPredicates = criteriaBuilder.and(allPredicates,
                    getTranslationPredicate(root, criteriaBuilder, searchCriteria));
            }
        }

        return allPredicates;
    }

    private Predicate getTranslationPredicate(Root<Tag> root,
        CriteriaBuilder criteriaBuilder, SearchCriteria searchCriteria) {
        ListJoin<Tag, TagTranslation> translationJoin = root.join(Tag_.tagTranslations);

        return searchCriteria.getValue().toString().trim().isEmpty() ? criteriaBuilder.conjunction()
            : criteriaBuilder.and(criteriaBuilder.like(translationJoin.get(TagTranslation_.NAME),
                "%" + searchCriteria.getValue() + "%"),
                criteriaBuilder.equal(translationJoin.get(TagTranslation_.tag).get(Tag_.ID),
                    root.get(Tag_.ID)));
    }
}
