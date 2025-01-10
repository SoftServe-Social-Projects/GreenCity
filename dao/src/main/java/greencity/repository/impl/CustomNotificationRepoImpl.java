package greencity.repository.impl;

import greencity.entity.Notification;
import greencity.entity.Notification_;
import greencity.entity.User;
import greencity.entity.User_;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import greencity.repository.CustomNotificationRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
public class CustomNotificationRepoImpl implements CustomNotificationRepo {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    /**
     * Initialization constructor.
     */
    public CustomNotificationRepoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Notification> findNotificationsByFilter(Long targetUserId, ProjectName projectName,
        List<NotificationType> notificationTypes, Boolean viewed, Pageable pageable) {
        CriteriaQuery<Notification> criteria = criteriaBuilder.createQuery(Notification.class);
        Root<Notification> notificationRoot = criteria.from(Notification.class);

        criteria.select(notificationRoot)
            .where(getPredicate(targetUserId, projectName, notificationTypes, viewed, notificationRoot))
            .orderBy(getOrders(notificationRoot));

        TypedQuery<Notification> typedQuery = entityManager.createQuery(criteria)
            .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
            .setMaxResults(pageable.getPageSize());
        List<Notification> resultList = typedQuery.getResultList();
        long total = getNotificationsCount(targetUserId, projectName, notificationTypes, viewed);

        return new PageImpl<>(resultList, pageable, total);
    }

    private Predicate getPredicate(Long targetUserId, ProjectName projectName, List<NotificationType> notificationTypes,
        Boolean viewed, Root<Notification> notificationRoot) {
        List<Predicate> predicates = new ArrayList<>();

        addTargetUserIdPredicate(targetUserId, notificationRoot, predicates);
        addProjectNamePredicate(projectName, notificationRoot, predicates);
        addNotificationTypesPredicate(notificationTypes, notificationRoot, predicates);
        addViewedPredicate(viewed, notificationRoot, predicates);

        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private void addTargetUserIdPredicate(Long targetUserId, Root<Notification> notificationRoot,
        List<Predicate> predicates) {
        Join<Notification, User> targetUserJoin = notificationRoot.join(Notification_.targetUser);
        predicates.add(criteriaBuilder.equal(targetUserJoin.get(User_.ID), targetUserId));
    }

    private void addProjectNamePredicate(ProjectName projectName, Root<Notification> notificationRoot,
        List<Predicate> predicates) {
        if (projectName != null) {
            predicates.add(criteriaBuilder.equal(notificationRoot.get(Notification_.PROJECT_NAME), projectName));
        }
    }

    private void addNotificationTypesPredicate(List<NotificationType> notificationTypes,
        Root<Notification> notificationRoot, List<Predicate> predicates) {
        if (notificationTypes != null && !notificationTypes.isEmpty()) {
            predicates.add(notificationRoot.get(Notification_.NOTIFICATION_TYPE).in(notificationTypes));
        }
    }

    private void addViewedPredicate(Boolean viewed, Root<Notification> notificationRoot, List<Predicate> predicates) {
        if (viewed != null) {
            predicates.add(criteriaBuilder.equal(notificationRoot.get(Notification_.VIEWED), viewed));
        }
    }

    private List<Order> getOrders(Root<Notification> notificationRoot) {
        ArrayList<Order> orders = new ArrayList<>();

        addSortByDateOrder(notificationRoot, orders);

        return orders;
    }

    private void addSortByDateOrder(Root<Notification> notificationRoot, ArrayList<Order> orders) {
        orders.add(criteriaBuilder.desc(notificationRoot.get(Notification_.TIME)));
    }

    private long getNotificationsCount(Long targetUserId, ProjectName projectName,
        List<NotificationType> notificationTypes, Boolean viewed) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Notification> countRoot = countQuery.from(Notification.class);

        countQuery.select(criteriaBuilder.count(countRoot))
            .where(getPredicate(targetUserId, projectName, notificationTypes, viewed, countRoot));
        return entityManager.createQuery(countQuery).getSingleResult();
    }
}
