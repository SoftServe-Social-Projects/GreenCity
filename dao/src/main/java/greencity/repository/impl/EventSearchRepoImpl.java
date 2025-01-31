package greencity.repository.impl;

import greencity.dto.filter.FilterEventDto;
import greencity.entity.Tag;
import greencity.entity.Tag_;
import greencity.entity.User;
import greencity.entity.User_;
import greencity.entity.event.Address;
import greencity.entity.event.Address_;
import greencity.entity.event.Event;
import greencity.entity.event.EventDateLocation;
import greencity.entity.event.EventDateLocation_;
import greencity.entity.event.Event_;
import greencity.entity.localization.TagTranslation;
import greencity.entity.localization.TagTranslation_;
import greencity.enums.EventStatus;
import greencity.enums.EventTime;
import greencity.enums.EventType;
import greencity.repository.EventSearchRepo;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.ListJoin;
import jakarta.persistence.criteria.Order;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.SetJoin;
import java.sql.Date;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

@Repository
public class EventSearchRepoImpl implements EventSearchRepo {
    private final EntityManager entityManager;
    private final CriteriaBuilder criteriaBuilder;

    /**
     * Initialization constructor.
     */
    public EventSearchRepoImpl(EntityManager entityManager) {
        this.entityManager = entityManager;
        this.criteriaBuilder = entityManager.getCriteriaBuilder();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Long> findEventsIds(Pageable pageable, FilterEventDto filterEventDto, Long userId) {
        CriteriaQuery<Long> criteria = criteriaBuilder.createQuery(Long.class);
        Root<Event> eventRoot = criteria.from(Event.class);

        criteria.select(eventRoot.get(Event_.ID))
            .where(getPredicate(filterEventDto, userId, eventRoot))
            .orderBy(getOrders(userId, eventRoot));

        List<Long> resultList = entityManager.createQuery(criteria).getResultList();
        List<Long> uniqueEventIds = resultList.stream().distinct().toList();

        return buildPage(uniqueEventIds, pageable);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Long> findEventsIdsManagement(Pageable pageable, FilterEventDto filterEventDto, Long userId) {
        CriteriaQuery<Long> criteria = criteriaBuilder.createQuery(Long.class);
        Root<Event> eventRoot = criteria.from(Event.class);

        criteria.select(eventRoot.get(Event_.ID))
            .where(getPredicate(filterEventDto, userId, eventRoot))
            .orderBy(getOrdersManagement(pageable.getSort(), eventRoot));

        List<Long> resultList = entityManager.createQuery(criteria).getResultList();
        List<Long> uniqueEventIds = resultList.stream().distinct().toList();

        return buildPage(uniqueEventIds, pageable);
    }

    private PageImpl<Long> buildPage(List<Long> uniqueEventIds, Pageable pageable) {
        int totalElements = uniqueEventIds.size();
        int fromIndex = pageable.getPageNumber() * pageable.getPageSize();
        int toIndex = Math.min(fromIndex + pageable.getPageSize(), totalElements);

        List<Long> pagedEventIds = fromIndex < totalElements
            ? uniqueEventIds.subList(fromIndex, toIndex)
            : new ArrayList<>();

        return new PageImpl<>(pagedEventIds, pageable, totalElements);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Page<Event> find(Pageable pageable, String searchingText, Boolean isFavorite, Long userId) {
        CriteriaQuery<Event> criteriaQuery = criteriaBuilder.createQuery(Event.class);
        Root<Event> root = criteriaQuery.from(Event.class);

        Predicate predicate = getPredicate(searchingText, isFavorite, userId, root);
        criteriaQuery.select(root).distinct(true).where(predicate);

        TypedQuery<Event> typedQuery = entityManager.createQuery(criteriaQuery)
            .setFirstResult(pageable.getPageNumber() * pageable.getPageSize())
            .setMaxResults(pageable.getPageSize());

        List<Event> resultList = typedQuery.getResultList();
        long total = getEventsCount(searchingText, isFavorite, userId);

        return new PageImpl<>(resultList, pageable, total);
    }

    private Predicate getPredicate(String searchingText, Boolean isFavorite, Long userId, Root<Event> root) {
        List<Predicate> predicates = new ArrayList<>();
        addEventsLikePredicate(searchingText, root, predicates);
        addIsFavoritePredicate(isFavorite, userId, root, predicates);
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private Predicate getPredicate(FilterEventDto filterEventDto, Long userId, Root<Event> eventRoot) {
        List<Predicate> predicates = new ArrayList<>();
        if (filterEventDto != null) {
            addEventTimePredicate(filterEventDto.getTime(), eventRoot, predicates);
            addCitiesPredicate(filterEventDto.getCities(), eventRoot, predicates);
            addStatusesPredicate(filterEventDto.getStatuses(), userId, eventRoot, predicates);
            addTagsPredicate(filterEventDto.getTags(), eventRoot, predicates);
            addTitlePredicate(filterEventDto.getTitle(), eventRoot, predicates);
            addTypePredicate(filterEventDto.getType(), eventRoot, predicates);
            addDatePredicate(filterEventDto, eventRoot, predicates);
        }
        return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
    }

    private void addEventTimePredicate(EventTime eventTime, Root<Event> eventRoot, List<Predicate> predicates) {
        if (eventTime != null) {
            ListJoin<Event, EventDateLocation> datesJoin = eventRoot.join(Event_.dates, JoinType.LEFT);
            if (eventTime == EventTime.UPCOMING) {
                predicates.add(
                    criteriaBuilder.greaterThan(datesJoin.get(EventDateLocation_.FINISH_DATE), ZonedDateTime.now()));
            } else if (eventTime == EventTime.PAST) {
                predicates.add(
                    criteriaBuilder.lessThan(datesJoin.get(EventDateLocation_.FINISH_DATE), ZonedDateTime.now()));
            }
        }
    }

    private void addCitiesPredicate(List<String> cities, Root<Event> eventRoot, List<Predicate> predicates) {
        if (cities != null && !cities.isEmpty()) {
            List<String> citiesInUpperCase = listToUpperCase(cities);
            Join<EventDateLocation, Address> addressJoin = eventRoot
                .join(Event_.dates, JoinType.LEFT).join(EventDateLocation_.address);
            predicates.add(criteriaBuilder.upper(addressJoin.get(Address_.CITY_EN)).in(citiesInUpperCase));
        }
    }

    private void addStatusesPredicate(List<EventStatus> eventStatuses, Long userId, Root<Event> eventRoot,
        List<Predicate> predicates) {
        if (eventStatuses != null) {
            List<Predicate> statusesPredicate = new ArrayList<>();
            eventStatuses.forEach(status -> {
                if (status == EventStatus.OPEN) {
                    statusesPredicate.add(criteriaBuilder.isTrue(eventRoot.get(Event_.IS_OPEN)));
                } else if (status == EventStatus.CLOSED) {
                    statusesPredicate.add(criteriaBuilder.isFalse(eventRoot.get(Event_.IS_OPEN)));
                } else if (status == EventStatus.CREATED && userId != null) {
                    Join<Event, User> organizerJoin = eventRoot.join(Event_.organizer, JoinType.LEFT);
                    statusesPredicate.add(criteriaBuilder.equal(organizerJoin.get(User_.ID), userId));
                } else if (status == EventStatus.JOINED && userId != null) {
                    SetJoin<Event, User> attendersJoin = eventRoot.join(Event_.attenders, JoinType.LEFT);
                    statusesPredicate.add(criteriaBuilder.equal(attendersJoin.get(User_.ID), userId));
                } else if (status == EventStatus.SAVED && userId != null) {
                    SetJoin<Event, User> followersJoin = eventRoot.join(Event_.followers, JoinType.LEFT);
                    statusesPredicate.add(criteriaBuilder.equal(followersJoin.get(User_.ID), userId));
                }
            });
            if (!statusesPredicate.isEmpty()) {
                predicates.add(criteriaBuilder.or(statusesPredicate.toArray(new Predicate[0])));
            }
        }
    }

    private void addTagsPredicate(List<String> tags, Root<Event> eventRoot, List<Predicate> predicates) {
        if (tags != null && !tags.isEmpty()) {
            List<String> tagsInUpperCase = listToUpperCase(tags);
            ListJoin<Tag, TagTranslation> tagsJoin = eventRoot.join(Event_.tags).join(Tag_.tagTranslations);
            predicates.add(criteriaBuilder.upper(tagsJoin.get(TagTranslation_.NAME)).in(tagsInUpperCase));
        }
    }

    private void addTitlePredicate(String title, Root<Event> eventRoot, List<Predicate> predicates) {
        if (title != null && !title.isEmpty()) {
            predicates.add(criteriaBuilder.like(
                criteriaBuilder.lower(eventRoot.get(Event_.TITLE)), "%" + title.toLowerCase() + "%"));
        }
    }

    private void addTypePredicate(EventType type, Root<Event> eventRoot, List<Predicate> predicates) {
        if (type != null) {
            predicates.add(criteriaBuilder.equal(eventRoot.get(Event_.TYPE), type));
        }
    }

    private void addDatePredicate(FilterEventDto filterEventDto, Root<Event> eventRoot, List<Predicate> predicates) {
        ZonedDateTime from = filterEventDto.getFrom();
        ZonedDateTime to = filterEventDto.getTo();
        ListJoin<Event, EventDateLocation> datesJoin = eventRoot.join(Event_.dates, JoinType.LEFT);
        Predicate finalPredicate = criteriaBuilder.conjunction();

        if (from != null && to != null) {
            Predicate dateRangePredicate = criteriaBuilder.and(
                criteriaBuilder.lessThanOrEqualTo(datesJoin.get(EventDateLocation_.START_DATE), to),
                criteriaBuilder.greaterThanOrEqualTo(datesJoin.get(EventDateLocation_.FINISH_DATE), from));
            finalPredicate = criteriaBuilder.and(finalPredicate, dateRangePredicate);
        } else if (from != null) {
            Predicate startDatePredicate =
                criteriaBuilder.greaterThanOrEqualTo(datesJoin.get(EventDateLocation_.START_DATE), from);
            finalPredicate = criteriaBuilder.and(finalPredicate, startDatePredicate);
        } else if (to != null) {
            Predicate finishDatePredicate =
                criteriaBuilder.lessThanOrEqualTo(datesJoin.get(EventDateLocation_.FINISH_DATE), to);
            finalPredicate = criteriaBuilder.and(finalPredicate, finishDatePredicate);
        }

        predicates.add(finalPredicate);
    }

    private void addEventsLikePredicate(String searchingText, Root<Event> root, List<Predicate> predicates) {
        ArrayList<Predicate> eventsLikePredicates = new ArrayList<>();
        Arrays.stream(searchingText.split(" ")).forEach(p -> eventsLikePredicates.add(
            criteriaBuilder.or(
                criteriaBuilder.like(criteriaBuilder.lower(root.get(Event_.TITLE)),
                    "%" + p.toLowerCase() + "%"),
                criteriaBuilder.like(criteriaBuilder.lower(root.get(Event_.DESCRIPTION)),
                    "%" + p.toLowerCase() + "%"))));
        predicates.add(criteriaBuilder.or(eventsLikePredicates.toArray(new Predicate[0])));
    }

    private void addIsFavoritePredicate(Boolean isFavorite, Long userId, Root<Event> root, List<Predicate> predicates) {
        if (isFavorite == null) {
            return;
        }
        SetJoin<Event, User> followersJoin = root.join(Event_.followers);
        if (Boolean.TRUE.equals(isFavorite)) {
            predicates.add(criteriaBuilder.equal(followersJoin.get(User_.ID), userId));
        } else {
            predicates.add(criteriaBuilder.notEqual(followersJoin.get(User_.ID), userId));
        }
    }

    private List<Order> getOrders(Long userId, Root<Event> eventRoot) {
        List<Order> orders = new ArrayList<>();

        if (userId != null) {
            addSortByOrganizerOrder(userId, eventRoot, orders);
            addSortByFollowersOrder(userId, eventRoot, orders);
            addSortByAttendersOrder(userId, eventRoot, orders);
        }

        ListJoin<Event, EventDateLocation> datesJoin = eventRoot.join(Event_.dates, JoinType.LEFT);
        addSortByCurrentDateOrder(orders, datesJoin);
        addSortByOneWeekOrder(orders, datesJoin);
        addSortByDateOrder(orders, datesJoin);

        return orders;
    }

    private List<Order> getOrdersManagement(Sort sort, Root<Event> eventRoot) {
        List<Order> orders = new ArrayList<>();

        if (sort != null && !sort.isEmpty()) {
            for (Sort.Order order : sort) {
                if (order.isAscending()) {
                    orders.add(criteriaBuilder.asc(eventRoot.get(order.getProperty())));
                } else {
                    orders.add(criteriaBuilder.desc(eventRoot.get(order.getProperty())));
                }
            }
        }

        return orders;
    }

    private void addSortByOrganizerOrder(Long userId, Root<Event> eventRoot, List<Order> orders) {
        orders.add(criteriaBuilder.desc(criteriaBuilder.selectCase()
            .when(criteriaBuilder.equal(
                eventRoot.get(Event_.organizer).get(User_.ID), userId), 1)
            .otherwise(0)));
    }

    private void addSortByFollowersOrder(Long userId, Root<Event> eventRoot, List<Order> orders) {
        orders.add(criteriaBuilder.desc(criteriaBuilder.selectCase()
            .when(criteriaBuilder.equal(
                eventRoot.join(Event_.followers, JoinType.LEFT).get(User_.ID), userId), 1)
            .otherwise(0)));
    }

    private void addSortByAttendersOrder(Long userId, Root<Event> eventRoot, List<Order> orders) {
        orders.add(criteriaBuilder.desc(criteriaBuilder.selectCase()
            .when(criteriaBuilder.equal(
                eventRoot.join(Event_.attenders, JoinType.LEFT).get(User_.ID), userId), 1)
            .otherwise(0)));
    }

    private void addSortByOneWeekOrder(List<Order> orders, ListJoin<Event, EventDateLocation> datesJoin) {
        ZonedDateTime currentDate = ZonedDateTime.now();
        ZonedDateTime oneWeekLater = currentDate.plusWeeks(1);
        orders.add(criteriaBuilder.desc(criteriaBuilder.selectCase()
            .when(criteriaBuilder.and(
                criteriaBuilder.greaterThanOrEqualTo(datesJoin.get(EventDateLocation_.START_DATE), currentDate),
                criteriaBuilder.lessThanOrEqualTo(datesJoin.get(EventDateLocation_.START_DATE), oneWeekLater)), 1)
            .otherwise(0)));
    }

    private void addSortByCurrentDateOrder(List<Order> orders, ListJoin<Event, EventDateLocation> datesJoin) {
        orders.add(criteriaBuilder.desc(criteriaBuilder.selectCase()
            .when(criteriaBuilder.equal(
                criteriaBuilder.function("DATE", Date.class, datesJoin.get(EventDateLocation_.START_DATE)),
                criteriaBuilder.function("DATE", Date.class, criteriaBuilder.currentDate())), 1)
            .otherwise(0)));
    }

    private void addSortByDateOrder(List<Order> orders, ListJoin<Event, EventDateLocation> datesJoin) {
        orders.add(criteriaBuilder.desc(datesJoin.get(EventDateLocation_.START_DATE)));
    }

    private long getEventsCount(String searchingText, Boolean isFavorite, Long userId) {
        CriteriaQuery<Long> countQuery = criteriaBuilder.createQuery(Long.class);
        Root<Event> countRoot = countQuery.from(Event.class);

        countQuery.select(criteriaBuilder.count(countRoot))
            .where(getPredicate(searchingText, isFavorite, userId, countRoot));
        return entityManager.createQuery(countQuery).getSingleResult();
    }

    private List<String> listToUpperCase(List<String> objects) {
        return objects.stream()
            .map(String::toUpperCase)
            .toList();
    }
}
