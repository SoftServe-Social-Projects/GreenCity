package greencity.repository.options;

import greencity.constant.RepoConstants;
import greencity.dto.filter.FilterDiscountDto;
import greencity.dto.filter.FilterPlaceDto;
import greencity.dto.location.MapBoundsDto;
import greencity.entity.FavoritePlace;
import greencity.entity.Place;
import greencity.entity.User;
import greencity.enums.PlaceStatus;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.domain.Specification;

/**
 * The class implements {@link Specification}. Each constructor takes a
 * {@code DTO} class the type of which determines the further creation of a new
 * {@link Predicate} object.
 */
@RequiredArgsConstructor
public class PlaceFilter implements Specification<Place> {
    private final transient FilterPlaceDto filterPlaceDto;
    private final transient Long userId;

    public PlaceFilter(FilterPlaceDto filterPlaceDto) {
        this.filterPlaceDto = filterPlaceDto;
        this.userId = null;
    }

    /**
     * {@inheritDoc} Forms a list of {@link Predicate} based on type of the classes
     * initialized in the constructors.
     */
    @Override
    public Predicate toPredicate(Root<Place> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
        query.groupBy(root.get(RepoConstants.ID));
        List<Predicate> predicates = new ArrayList<>();
        if (null != filterPlaceDto) {
            predicates.add(hasStatus(root, cb, filterPlaceDto.getStatus()));
            predicates.add(hasPositionInBounds(root, cb, filterPlaceDto.getMapBoundsDto()));
            predicates.add(hasDiscount(root, cb, filterPlaceDto.getDiscountDto()));
            predicates.add(isNowOpen(root, cb, filterPlaceDto.getTime()));
            predicates.add(hasFieldLike(root, cb, filterPlaceDto.getSearchReg()));
            predicates.add(hasCategory(root, cb, filterPlaceDto.getCategories()));
            predicates.add(isSaved(root, cb, filterPlaceDto.getIsSaved(), userId));
        }
        return cb.and(predicates.toArray(new Predicate[0]));
    }

    /**
     * Returns a predicate where {@link PlaceStatus} of {@link Place} is equal to
     * {@param status} value.
     *
     * @param r      must not be {@literal null}.
     * @param cb     must not be {@literal null}.
     * @param status of {@link Place} to filter by.
     * @return a {@link Predicate}, may be {@literal null}.
     */
    private Predicate hasStatus(Root<Place> r, CriteriaBuilder cb, PlaceStatus status) {
        if (status == null) {
            status = PlaceStatus.APPROVED;
        }
        return cb.equal(r.get(RepoConstants.PLACE_STATUS), status);
    }

    /**
     * Returns a predicate where Category of {@link Place} is member of
     * {@param categories} array.
     *
     * @param r          must not be {@literal null}.
     * @param cb         must not be {@literal null}.
     * @param categories of {@link Place}'s to filter by.
     * @return a {@link Predicate} may be {@literal null}.
     */
    private Predicate hasCategory(Root<Place> r, CriteriaBuilder cb, String[] categories) {
        if (categories == null) {
            return cb.conjunction();
        } else {
            List<Predicate> predicates = new ArrayList<>();
            Arrays.stream(categories).forEach(c -> predicates
                .add(cb.like(r.join(RepoConstants.CATEGORY).get(RepoConstants.NAME), c)));
            return cb.or(predicates.toArray(new Predicate[0]));
        }
    }

    /**
     * Returns a predicate where favorite place is saved.
     *
     * @param root    must not be {@literal null}.
     * @param cb      must not be {@literal null}.
     * @param isSaved is saved place or not.
     * @param userId  user id.
     * @return a {@link Predicate}.
     */
    private Predicate isSaved(Root<Place> root, CriteriaBuilder cb, Boolean isSaved, Long userId) {
        if (isSaved == null || userId == null) {
            return cb.conjunction();
        } else {
            Join<Place, FavoritePlace> favoriteJoin = root.join(RepoConstants.FAVORITE_PLACES, JoinType.LEFT);
            Join<FavoritePlace, User> userJoin = favoriteJoin.join(RepoConstants.USER);
            return cb.equal(userJoin.get(RepoConstants.ID), userId);
        }
    }

    /**
     * Returns a predicate where {@link greencity.entity.Location}'s lat and lng are
     * in bounds of {@param bounds}.
     *
     * @param r      must not be {@literal null}.
     * @param cb     must not be {@literal null}.
     * @param bounds dto should contain lat and lng bounds values.
     * @return a {@link Predicate}, may be {@literal null}.
     */
    private Predicate hasPositionInBounds(Root<Place> r, CriteriaBuilder cb, MapBoundsDto bounds) {
        if (bounds == null) {
            return cb.conjunction();
        }
        return cb.and(
            cb.between(r.join(RepoConstants.LOCATION).get(RepoConstants.LOCATION_LAT),
                bounds.getSouthWestLat(), bounds.getNorthEastLat()),
            cb.between(r.join(RepoConstants.LOCATION).get(RepoConstants.LOCATION_LNG),
                bounds.getSouthWestLng(), bounds.getNorthEastLng()));
    }

    /**
     * Checks if {@link Place} is open at the time described in the
     * {@code currentTime} string argument. The method can throw a
     * {@link DateTimeParseException} if the {@code currentTime} string doesn't
     * match a {@code AppConstant.DATE_FORMAT} format string.
     *
     * @param r           must not be {@literal null}.
     * @param cb          must not be {@literal null}.
     * @param currentTime a string contains current date and time.
     */
    private Predicate isNowOpen(Root<Place> r, CriteriaBuilder cb, String currentTime) {
        if (null == currentTime) {
            return cb.conjunction();
        }
        LocalDateTime time = LocalDateTime.parse(currentTime, DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"));
        return cb.and(cb.equal(r.join(RepoConstants.HOURS_LIST).get(RepoConstants.HOURS_DAY), time.getDayOfWeek()),
            cb.lessThan(r.join(RepoConstants.HOURS_LIST).get(RepoConstants.HOURS_OPEN), time.toLocalTime()),
            cb.greaterThan(r.join(RepoConstants.HOURS_LIST).get(RepoConstants.HOURS_CLOSE), time.toLocalTime()));
    }

    /**
     * Returns a predicate where {@link Place} has some values defined in the
     * incoming {@link FilterDiscountDto} object.
     *
     * @param r        must not be {@literal null}.
     * @param cb       must not be {@literal null}.
     * @param discount a dto describes information about discount of a
     *                 {@link Place}.
     * @return a {@link Predicate}, may be {@literal null}.
     */
    private Predicate hasDiscount(Root<Place> r, CriteriaBuilder cb, FilterDiscountDto discount) {
        if (discount == null) {
            return cb.conjunction();
        }
        int minValue = discount.getDiscountMin();
        int maxValue = discount.getDiscountMax();
        if (minValue == 0 && maxValue == 100) {
            return cb.conjunction();
        }
        return cb.and(
            cb.equal(r.join(RepoConstants.DISCOUNT_VALUES).join(RepoConstants.SPECIFICATION).get(RepoConstants.NAME),
                discount.getSpecification().getName()),
            cb.between(r.join(RepoConstants.DISCOUNT_VALUES).get(RepoConstants.VALUE), minValue, maxValue));
    }

    /**
     * Returns a predicate where {@link Place} has some values defined in the
     * incoming {@link FilterPlaceDto} object.
     *
     * @param r  must not be {@literal null}.
     * @param cb must not be {@literal null}.
     * @return a {@link Predicate}, may be {@literal null}.
     */
    private Predicate hasFieldLike(Root<Place> r, CriteriaBuilder cb, String reg) {
        if (filterPlaceDto.getSearchReg() == null) {
            return cb.conjunction();
        }
        return cb.or(
            cb.like(r.join(RepoConstants.AUTHOR).get(RepoConstants.EMAIL), "%" + reg + "%"),
            cb.like(r.join(RepoConstants.CATEGORY).get(RepoConstants.NAME), "%" + reg + "%"),
            cb.like(r.get(RepoConstants.NAME), "%" + reg + "%"),
            cb.like(r.join(RepoConstants.LOCATION).get(RepoConstants.ADDRESS), "%" + reg + "%"),
            cb.like(r.get(RepoConstants.MODIFIED_DATE).as(String.class), "%" + reg + "%"));
    }
}
