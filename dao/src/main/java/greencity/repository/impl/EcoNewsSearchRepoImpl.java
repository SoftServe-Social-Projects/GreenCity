package greencity.repository.impl;

import greencity.entity.EcoNews;
import greencity.entity.EcoNews_;
import greencity.entity.User;
import greencity.entity.User_;
import greencity.repository.EcoNewsSearchRepo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Repository
public class EcoNewsSearchRepoImpl implements EcoNewsSearchRepo {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    /**
     * Initialization constructor.
     */
    public EcoNewsSearchRepoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<EcoNews> find(Pageable pageable, String searchingText, Boolean isFavorite, Long userId) {
        CriteriaQuery<EcoNews> criteriaQuery = criteriaBuilder.createQuery(EcoNews.class);
        Root<EcoNews> root = criteriaQuery.from(EcoNews.class);

        Predicate predicate = getPredicate(searchingText, isFavorite, userId, root);
        criteriaQuery.select(root).distinct(true).where(predicate);

        TypedQuery<EcoNews> typedQuery = entityManager.createQuery(criteriaQuery)
            .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
            .setMaxResults(pageable.getPageSize());

        List<EcoNews> resultList = typedQuery.getResultList();
        long total = getEcoNewsCount(searchingText, isFavorite, userId);

        return new PageImpl<>(resultList, pageable, total);
    }

    private long getEcoNewsCount(String searchingText, Boolean isFavorite, Long userId) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<EcoNews> countRoot = countQuery.from(EcoNews.class);

        countQuery.select(criteriaBuilder.count(countRoot))
            .where(getPredicate(searchingText, isFavorite, userId, countRoot));

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private Predicate getPredicate(String searchingText, Boolean isFavorite, Long userId, Root<EcoNews> root) {
        List<Predicate> predicates = new ArrayList<>();
        addEcoNewsLikePredicate(searchingText, root, predicates);
        addIsFavoritePredicate(isFavorite, userId, root, predicates);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private void addEcoNewsLikePredicate(String searchingText, Root<EcoNews> root, List<Predicate> predicates) {
        Expression<String> title = root.get("title").as(String.class);
        Expression<String> text = root.get("text").as(String.class);
        Expression<String> shortInfo = root.get("shortInfo").as(String.class);

        List<Predicate> predicateList = new ArrayList<>();
        Arrays.stream(searchingText.split(" ")).forEach(partOfSearchingText -> predicateList.add(
            criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(title), "%" + partOfSearchingText.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(text), "%" + partOfSearchingText.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(shortInfo),
                    "%" + partOfSearchingText.toLowerCase() + "%"))));
        predicates.add(criteriaBuilder.or(predicateList.toArray(new Predicate[0])));
    }

    private void addIsFavoritePredicate(Boolean isFavorite, Long userId, Root<EcoNews> root,
        List<Predicate> predicates) {
        if (isFavorite == null) {
            return;
        }
        SetJoin<EcoNews, User> followersJoin = root.join(EcoNews_.followers);
        if (Boolean.TRUE.equals(isFavorite)) {
            predicates.add(criteriaBuilder.equal(followersJoin.get(User_.ID), userId));
        } else {
            predicates.add(criteriaBuilder.notEqual(followersJoin.get(User_.ID), userId));
        }
    }
}
