package greencity.repository.impl;

import greencity.entity.Category;
import greencity.entity.Category_;
import greencity.entity.Location;
import greencity.entity.Location_;
import greencity.entity.Place;
import greencity.entity.Place_;
import greencity.repository.PlaceSearchRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class PlaceSearchRepoImpl implements PlaceSearchRepo {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    /**
     * Initialization constructor.
     */
    public PlaceSearchRepoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Place> find(Pageable pageable, String searchingText) {
        CriteriaQuery<Place> criteriaQuery = criteriaBuilder.createQuery(Place.class);
        Root<Place> root = criteriaQuery.from(Place.class);

        Predicate predicate = getPredicate(searchingText, root);
        criteriaQuery.select(root).distinct(true).where(predicate);

        TypedQuery<Place> typedQuery = entityManager.createQuery(criteriaQuery)
            .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
            .setMaxResults(pageable.getPageSize());

        List<Place> resultList = typedQuery.getResultList();
        long total = getPlacesCount(searchingText);

        return new PageImpl<>(resultList, pageable, total);
    }

    private Predicate getPredicate(String searchingText, Root<Place> root) {
        List<Predicate> predicates = new ArrayList<>();
        addPlacesLikePredicate(searchingText, root, predicates);
        return criteriaBuilder.or(predicates.toArray(new Predicate[0]));
    }

    private void addPlacesLikePredicate(String searchingText, Root<Place> root, List<Predicate> predicates) {
        Join<Place, Location> locationJoin = root.join(Place_.location, JoinType.LEFT);
        Join<Place, Category> categoryJoin = root.join(Place_.category, JoinType.LEFT);
        Arrays.stream(searchingText.split(" ")).forEach(p -> predicates.add(
            criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get(Place_.NAME)),
                    "%" + p.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(root.get(Place_.DESCRIPTION)),
                    "%" + p.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(locationJoin.get(Location_.ADDRESS)),
                    "%" + p.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(locationJoin.get(Location_.ADDRESS_UA)),
                    "%" + p.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(categoryJoin.get(Category_.NAME)),
                    "%" + p.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(categoryJoin.get(Category_.NAME_UA)),
                    "%" + p.toLowerCase() + "%"))));
    }

    private long getPlacesCount(String searchingText) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Place> countRoot = countQuery.from(Place.class);

        countQuery.select(criteriaBuilder.count(countRoot)).where(getPredicate(searchingText, countRoot));
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
