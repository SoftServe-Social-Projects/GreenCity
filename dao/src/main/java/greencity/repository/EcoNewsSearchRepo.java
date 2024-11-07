package greencity.repository;

import greencity.entity.EcoNews;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
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
public class EcoNewsSearchRepo {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    /**
     * Initialization constructor.
     */
    public EcoNewsSearchRepo(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * Method for search eco news by title,text,short info and tag name.
     *
     * @param searchingText - text criteria for searching.
     * @param languageCode  - code of needed language for finding tag.
     *
     * @return all finding eco news, their tags and also count of finding eco news.
     */
    public Page<EcoNews> find(Pageable pageable, String searchingText, String languageCode) {
        CriteriaQuery<EcoNews> criteriaQuery = criteriaBuilder.createQuery(EcoNews.class);
        Root<EcoNews> root = criteriaQuery.from(EcoNews.class);

        Predicate predicate = getPredicate(criteriaBuilder, searchingText, languageCode, root);
        criteriaQuery.select(root).distinct(true).where(predicate);

        TypedQuery<EcoNews> typedQuery = entityManager.createQuery(criteriaQuery)
            .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
            .setMaxResults(pageable.getPageSize());

        List<EcoNews> resultList = typedQuery.getResultList();
        long total = getEcoNewsCount(criteriaBuilder, searchingText, languageCode);

        return new PageImpl<>(resultList, pageable, total);
    }

    private long getEcoNewsCount(CriteriaBuilder criteriaBuilder, String searchingText, String languageCode) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<EcoNews> countRoot = countQuery.from(EcoNews.class);

        Predicate countPredicate = getPredicate(criteriaBuilder, searchingText, languageCode, countRoot);
        countQuery.select(criteriaBuilder.count(countRoot)).where(countPredicate);

        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<Predicate> formEcoNewsLikePredicate(CriteriaBuilder criteriaBuilder, String searchingText,
        Root<EcoNews> root) {
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
        return predicateList;
    }

    private Predicate formTagTranslationsPredicate(CriteriaBuilder criteriaBuilder, String searchingText,
        String languageCode, Root<EcoNews> root) {
        CriteriaQuery<Long> criteriaQuery = criteriaBuilder.createQuery(Long.class);
        Subquery<Long> tagSubquery = criteriaQuery.subquery(Long.class);
        Root<Tag> tagRoot = tagSubquery.from(Tag.class);
        Join<EcoNews, Tag> ecoNewsTagJoin = tagRoot.join("ecoNews");

        Subquery<Long> tagTranslationSubquery = criteriaQuery.subquery(Long.class);
        Root<Tag> tagTranslationRoot = tagTranslationSubquery.correlate(tagRoot);

        Join<TagTranslation, Tag> tagTranslationTagJoin = tagTranslationRoot.join("tagTranslations");

        Predicate predicate = predicateForTags(criteriaBuilder, searchingText, languageCode, tagTranslationTagJoin);
        tagTranslationSubquery.select(tagTranslationTagJoin.get("id"))
            .where(predicate);

        tagSubquery.select(ecoNewsTagJoin.get("id")).where(criteriaBuilder.exists(tagTranslationSubquery));
        return criteriaBuilder.in(root.get("id")).value(tagSubquery);
    }

    private Predicate predicateForTags(CriteriaBuilder criteriaBuilder, String searchingText, String languageCode,
        Join<TagTranslation, Tag> tagTranslationTagJoin) {
        List<Predicate> predicateList = new ArrayList<>();
        Arrays.stream(searchingText.split(" ")).forEach(partOfSearchingText -> predicateList.add(criteriaBuilder.and(
            criteriaBuilder.like(criteriaBuilder.lower(tagTranslationTagJoin.get("name")),
                "%" + partOfSearchingText.toLowerCase() + "%"),
            criteriaBuilder.like(criteriaBuilder.lower(tagTranslationTagJoin.get("language").get("code")),
                "%" + languageCode.toLowerCase() + "%"))));
        return criteriaBuilder.or(predicateList.toArray(new Predicate[0]));
    }

    private Predicate getPredicate(CriteriaBuilder criteriaBuilder, String searchingText,
        String languageCode, Root<EcoNews> root) {
        List<Predicate> predicateList = formEcoNewsLikePredicate(criteriaBuilder, searchingText, root);
        predicateList.add(formTagTranslationsPredicate(criteriaBuilder, searchingText, languageCode, root));
        return criteriaBuilder.or(predicateList.toArray(new Predicate[0]));
    }
}