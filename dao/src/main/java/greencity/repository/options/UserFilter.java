package greencity.repository.options;

import greencity.constant.RepoConstants;
import greencity.dto.user.UserFilterDto;
import greencity.entity.User;
import static greencity.repository.options.CriteriaUtils.replaceCriteria;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * The class implements {@link Specification}. Constructor takes a {@code DTO}
 * class the type of which determines the further creation of a new
 * {@link Predicate} object.
 *
 * @author Rostyslav Khasanov
 */
@RequiredArgsConstructor
public class UserFilter implements Specification<User> {
    private final transient UserFilterDto filterUserDto;

    /**
     * Forms a list of {@link Predicate} based on type of the classes initialized in
     * the constructors.
     */
    @Override
    public Predicate toPredicate(Root<User> root, CriteriaQuery<?> criteriaQuery, CriteriaBuilder criteriaBuilder) {
        List<Predicate> predicates = new ArrayList<>();

        if (filterUserDto != null) {
            predicates.add(hasFieldsLike(root, criteriaBuilder, filterUserDto.getQuery()));
        }
        if (filterUserDto != null && filterUserDto.getStatus() != null) {
            predicates.add(hasStatusLike(root, criteriaBuilder, filterUserDto.getStatus()));
        }
        if (filterUserDto != null && filterUserDto.getRole() != null) {
            predicates.add(hasRoleLike(root, criteriaBuilder, filterUserDto.getRole()));
        }

        return criteriaBuilder.and(predicates.toArray(new Predicate[] {}));
    }

    /**
     * Returns a predicate where {@link User} has some values defined in the
     * incoming {@link UserFilterDto} object.
     *
     * @param r  must not be {@literal null}.
     * @param cb must not be {@literal null}.
     * @return a {@link Predicate}, may be {@literal null}.
     */
    private Predicate hasFieldsLike(Root<User> r, CriteriaBuilder cb, String reg) {
        reg = replaceCriteria(reg);
        return cb.or(
            cb.like(r.get(RepoConstants.NAME), reg),
            cb.like(r.get(RepoConstants.EMAIL), reg),
            cb.like(r.get(RepoConstants.REGISTRATION_DATE).as(String.class), reg));
    }

    /**
     * return a predicate where {@link User} has status defined in the incoming
     * object.
     *
     * @param r      must not be {@literal null}.
     * @param cb     must not be {@literal null}.
     * @param status status which defined in object.
     * @return a {@link Predicate}, may be {@literal null}.
     */
    private Predicate hasStatusLike(Root<User> r, CriteriaBuilder cb, String status) {
        status = replaceCriteria(status);
        return cb.or(cb.like(r.get(RepoConstants.STATUS).as(String.class), status));
    }

    /**
     * return a predicate where {@link User} has status defined in the incoming
     * object.
     *
     * @param r    must not be {@literal null}.
     * @param cb   must not be {@literal null}.
     * @param role role which defined in object.
     * @return a {@link Predicate}, may be {@literal null}.
     */
    private Predicate hasRoleLike(Root<User> r, CriteriaBuilder cb, String role) {
        role = replaceCriteria(role);
        return cb.like(r.get(RepoConstants.ROLE).as(String.class), role);
    }
}
