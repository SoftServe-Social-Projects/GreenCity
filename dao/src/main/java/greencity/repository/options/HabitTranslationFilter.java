package greencity.repository.options;

import greencity.dto.filter.HabitTranslationFilterDto;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import greencity.enums.HabitAssignStatus;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.jpa.domain.Specification;
import java.util.ArrayList;
import java.util.List;

/**
 * The class implements {@link Specification} for filtering
 * {@link HabitTranslation} entities based on various criteria encapsulated in
 * the {@link HabitTranslationFilterDto} DTO. The filtering logic includes
 * checking custom habits, language codes, complexity levels, and associated
 * tags.
 *
 * @author Nazar Prots
 */
@RequiredArgsConstructor
public class HabitTranslationFilter implements Specification<HabitTranslation> {
    private final transient HabitTranslationFilterDto filter;

    /**
     * Forms a list of {@link Predicate} based on type of the classes initialized in
     * the constructors.
     */
    @Override
    public Predicate toPredicate(Root<HabitTranslation> root, CriteriaQuery<?> query, CriteriaBuilder criteriaBuilder) {
        query.distinct(true);
        query.orderBy(criteriaBuilder.desc(root.get("id")));
        List<Predicate> predicates = createPredicates(root, criteriaBuilder);
        return criteriaBuilder.and(predicates.toArray(new Predicate[] {}));
    }

    /**
     * Constructs the list of predicates based on the filtering conditions.
     */
    private List<Predicate> createPredicates(Root<HabitTranslation> root, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        Join<HabitTranslation, Habit> habitJoin = root.join("habit", JoinType.INNER);

        if (filter.getIsCustom() == null) {
            predicates.add(
                criteriaBuilder.or(
                    createNonCustomHabitPredicate(criteriaBuilder, habitJoin),
                    createCustomHabitPredicate(criteriaBuilder, habitJoin, filter.getUserId())));
        }
        if (Boolean.FALSE.equals(filter.getIsCustom())) {
            predicates.add(createNonCustomHabitPredicate(criteriaBuilder, habitJoin));
        }

        if (Boolean.TRUE.equals(filter.getIsCustom()) && filter.getUserId() != null) {
            predicates.add(createCustomHabitPredicate(criteriaBuilder, habitJoin, filter.getUserId()));
        }

        if (StringUtils.isNotEmpty(filter.getLanguageCode())) {
            predicates.add(createLanguageCodePredicate(root, criteriaBuilder, filter.getLanguageCode()));
        }

        if (CollectionUtils.isNotEmpty(filter.getComplexities())) {
            predicates.add(createComplexityPredicate(filter.getComplexities(), habitJoin));
        }

        if (CollectionUtils.isNotEmpty(filter.getTags())) {
            predicates.add(createTagsPredicate(criteriaBuilder, habitJoin, filter.getTags()));
        }

        return predicates;
    }

    private Predicate createNonCustomHabitPredicate(CriteriaBuilder cb, Join<HabitTranslation, Habit> habitJoin) {
        Predicate isCustomHabitFalse = cb.equal(habitJoin.get("isCustomHabit"), false);
        Predicate isDeletedFalse = cb.equal(habitJoin.get("isDeleted"), false);
        return cb.and(isCustomHabitFalse, isDeletedFalse);
    }

    private Predicate createCustomHabitPredicate(CriteriaBuilder cb, Join<HabitTranslation, Habit> habitJoin,
        Long userId) {
        Subquery<Long> subquery = cb.createQuery().subquery(Long.class);
        Root<HabitAssign> habitAssignRoot = subquery.from(HabitAssign.class);
        subquery.select(habitAssignRoot.get("habit").get("id"))
            .where(
                cb.equal(habitAssignRoot.get("user").get("id"), userId),
                cb.equal(cb.upper(habitAssignRoot.get("status")), HabitAssignStatus.REQUESTED.name()));

        Predicate isCustomHabitTrue = cb.equal(habitJoin.get("isCustomHabit"), true);
        Predicate isDeletedFalse = cb.equal(habitJoin.get("isDeleted"), false);

        Predicate isRequested = habitJoin.get("id").in(subquery);
        Predicate isAuthor = cb.equal(habitJoin.get("userId"), userId);

        return cb.and(
            isCustomHabitTrue,
            isDeletedFalse,
            cb.or(isRequested, isAuthor));
    }

    private Predicate createTagsPredicate(CriteriaBuilder cb, Join<HabitTranslation, Habit> habitJoin,
        List<String> tags) {
        Join<Habit, Tag> tagJoin = habitJoin.join("tags", JoinType.INNER);
        Join<Tag, TagTranslation> tagTranslationJoin = tagJoin.join("tagTranslations", JoinType.INNER);

        List<String> lowerCaseTags = tags.stream()
            .map(String::toLowerCase)
            .toList();

        return cb.lower(tagTranslationJoin.get("name")).in(lowerCaseTags);
    }

    private Predicate createComplexityPredicate(List<Integer> complexities, Join<HabitTranslation, Habit> habitJoin) {
        return habitJoin.get("complexity").in(complexities);
    }

    private Predicate createLanguageCodePredicate(Root<HabitTranslation> root, CriteriaBuilder cb,
        String languageCode) {
        Join<HabitTranslation, Language> languageJoin = root.join("language", JoinType.INNER);
        return cb.equal(languageJoin.get("code"), languageCode);
    }
}
