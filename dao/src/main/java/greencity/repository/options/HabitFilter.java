package greencity.repository.options;

import greencity.dto.filter.FilterHabitDto;
import greencity.entity.*;
import static greencity.repository.options.CriteriaUtils.replaceCriteria;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * The class implements {@link Specification}. Constructor takes a {@code DTO}
 * class the type of which determines the further creation of a new
 * {@link Predicate} object.
 *
 * @author Hutei Volodymyr
 */
@RequiredArgsConstructor
public class HabitFilter implements Specification<Habit> {
    private static final String DEFAULT_HABIT_IMAGE =
        "https://csb10032000a548f571.blob.core.windows.net/allfiles/photo_"
            + "2021-06-01_15-39-56.jpg";
    private final transient FilterHabitDto filterHabitDto;

    /**
     * Forms a list of {@link Predicate} based on type of the classes initialized in
     * the constructors.
     */
    @Override
    public Predicate toPredicate(Root<Habit> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();
        criteriaQuery.distinct(true);

        if (filterHabitDto != null) {
            predicates.add(hasFieldsLike(root, criteriaBuilder, filterHabitDto.getSearchReg()));
        }
        if (filterHabitDto != null && filterHabitDto.getDurationFrom() != null
            && filterHabitDto.getDurationTo() != null) {
            predicates.add(hasDurationBetween(root, criteriaBuilder,
                filterHabitDto.getDurationFrom(), filterHabitDto.getDurationTo()));
        }
        if (filterHabitDto != null && filterHabitDto.getComplexity() != null) {
            predicates.add(hasComplexityEquals(root, criteriaBuilder, filterHabitDto.getComplexity()));
        }
        if (filterHabitDto != null
            && (filterHabitDto.isWithImage() ^ filterHabitDto.isWithoutImage())) {
            predicates.add(hasImage(root, criteriaBuilder, filterHabitDto.isWithoutImage(),
                filterHabitDto.isWithImage()));
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[] {}));
    }

    /**
     * Returns a predicate where {@link Habit} has some values defined in the
     * incoming {@link FilterHabitDto} object.
     *
     * @param r  must not be {@literal null}.
     * @param cb must not be {@literal null}.
     * @return a {@link Predicate}, may be {@literal null}.
     */
    private Predicate hasFieldsLike(Root<Habit> r, CriteriaBuilder cb, String reg) {
        reg = replaceCriteria(reg);
        Join<Habit, HabitTranslation> habitTranslations = r.join(Habit_.habitTranslations, JoinType.INNER);

        return cb.or(
            cb.like(r.get("id").as(String.class), reg),
            cb.like(r.get("defaultDuration").as(String.class), reg),
            cb.like(r.get("complexity").as(String.class), reg),
            cb.like(habitTranslations.get("description"), reg),
            cb.like(habitTranslations.get("habitItem"), reg),
            cb.like(habitTranslations.get("name"), reg));
    }

    private Predicate hasImage(Root<Habit> r, CriteriaBuilder cb, boolean withoutImage, boolean withImage) {
        if (withoutImage) {
            return cb.like(r.get("image"), DEFAULT_HABIT_IMAGE);
        }
        if (withImage) {
            return cb.notLike(r.get("image"), DEFAULT_HABIT_IMAGE);
        }
        return null;
    }

    private Predicate hasDurationBetween(Root<Habit> r, CriteriaBuilder cb, Integer durationFrom, Integer durationTo) {
        return cb.between(r.get("defaultDuration"), durationFrom, durationTo);
    }

    private Predicate hasComplexityEquals(Root<Habit> r, CriteriaBuilder cb, Integer complexity) {
        return cb.equal(r.get("complexity"), complexity);
    }
}
