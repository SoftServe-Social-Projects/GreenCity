package greencity.repository;

import greencity.entity.User;
import greencity.entity.event.Address;
import greencity.entity.event.Event;
import jakarta.persistence.Tuple;
import java.util.Collection;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

public interface EventRepo extends EventSearchRepo, JpaRepository<Event, Long>, JpaSpecificationExecutor<Event> {
    /**
     * Method returns {@link Event} by search query and page.
     *
     * @param paging {@link Pageable}.
     * @param query  query to search.
     * @return list of {@link Event}.
     */
    @Query(nativeQuery = true,
        value = "SELECT DISTINCT e.* "
            + "FROM events e "
            + "JOIN users u on u.id = e.organizer_id "
            + "JOIN events_tags ent on e.id = ent.event_id "
            + "JOIN events_dates_locations edl on e.id = edl.event_id "
            + "JOIN tag_translations tt on tt.tag_id = ent.tag_id "
            + "WHERE concat(e.id,'') like lower(concat(:query, '%')) or "
            + "    lower(e.title) like lower(concat('%', :query, '%')) or "
            + "    lower(e.description) like lower(concat('%', :query, '%')) or "
            + "    lower(u.name) like lower(concat('%', :query, '%')) or "
            + "    lower(tt.name) like lower(concat('%', :query, '%'))")
    Page<Event> searchEventsBy(Pageable paging, String query);

    /**
     * Remove event datesLocations.
     *
     * @param eventId {@link Long}.
     */
    @Modifying
    @Query(value = "DELETE FROM events_dates_locations WHERE event_id = :eventId", nativeQuery = true)
    void deleteEventDateLocationsByEventId(Long eventId);

    /**
     * Remove event additional images.
     *
     * @param eventId {@link Long}.
     */
    @Modifying
    @Query(value = "DELETE FROM events_images WHERE event_id = :eventId", nativeQuery = true)
    void deleteEventAdditionalImagesByEventId(Long eventId);

    /**
     * Get all events by event organizer.
     *
     * @param organizer {@link User}.
     */
    List<Event> getAllByOrganizer(User organizer);

    /**
     * Get subscribed events in given event ids by user id.
     */
    @Query(value = "SELECT e FROM Event e LEFT JOIN e.attenders AS f WHERE e.id in :eventIds AND f.id = :userId")
    List<Event> findSubscribedAmongEventIds(Collection<Long> eventIds, Long userId);

    /**
     * Get favorite events in given events by user id.
     */
    @Query(value = "SELECT e FROM Event e LEFT JOIN e.followers AS f WHERE e.id in :eventIds AND f.id = :userId")
    List<Event> findFavoritesAmongEventIds(Collection<Long> eventIds, Long userId);

    /**
     * Get all events addresses.
     */
    @Query(value = "SELECT DISTINCT edl.address FROM Event e JOIN e.dates edl WHERE edl.address IS NOT NULL")
    List<Address> findAllEventsAddresses();

    /**
     * Method returns count of all events where user is attender.
     *
     * @param attenderId {@link Long} id of current user.
     * @return {@link Long} count of attended by user events.
     */
    Long countDistinctByAttendersId(Long attenderId);

    /**
     * Method returns count of all events where user is organizer.
     *
     * @param organizerId {@link Long} id of current user.
     * @return {@link Long} count of organized by user events.
     */
    Long countDistinctByOrganizerId(Long organizerId);

    /**
     * Retrieves a list of event preview data for the given event IDs for
     * unauthorized user. Does not support order from entry list.
     *
     * @param ids A list of event IDs for which the preview data is to be retrieved.
     * @return A list of Tuples containing the event preview data for the specified
     *         IDs.
     */
    @Query(nativeQuery = true, value = """
        SELECT e.id AS eventId, e.title, e.description, et.tag_id AS tagId, l.code AS languageCode,
               tt.name AS tagName, e.is_open, u.id AS organizerId, u.name AS organizerName,
               e.title_image, e.creation_date, start_date, finish_date, online_link,
               latitude, longitude, street_en, street_ua, house_number, city_en, city_ua,
               region_en, region_ua, country_en, country_ua, formatted_address_en,
               formatted_address_ua, e.type,
               (CURRENT_DATE <= edl_max.latest_finish_date) AS isRelevant,
               COUNT(DISTINCT eul) AS likes,
               COUNT(DISTINCT eud) AS dislikes,
               COUNT(DISTINCT ec)  AS countComments,
               AVG(eg.grade)       AS grade,
               (false)             AS isOrganizedByFriend,
               (false)             AS isOrganizedByUser,
               (false)             AS isSubscribed,
               (false)             AS isFavorite,
                NULL AS currentUserGrade
        FROM events e
                 LEFT JOIN events_grades eg ON e.id = eg.event_id
                 LEFT JOIN comments ec ON e.id = ec.article_id AND ec.article_type = 'EVENT' AND ec.status != 'DELETED'
                 LEFT JOIN events_users_likes eul ON e.id = eul.event_id
                 LEFT JOIN events_users_dislikes eud ON e.id = eud.event_id
                 LEFT JOIN events_dates_locations edl ON e.id = edl.event_id
                 LEFT JOIN (
                    SELECT event_id, MAX(finish_date) AS latest_finish_date
                    FROM events_dates_locations
                    GROUP BY event_id
                 ) edl_max ON e.id = edl_max.event_id
                 LEFT JOIN events_tags et ON e.id = et.event_id
                 LEFT JOIN tag_translations tt ON et.tag_id = tt.tag_id
                 LEFT JOIN languages l ON tt.language_id = l.id
                 LEFT JOIN users u ON e.organizer_id = u.id
        WHERE (e.id IN (:ids))
        GROUP BY e.id, tt.name, edl.city_en, et.tag_id, l.code, u.id, edl.id, edl_max.latest_finish_date;""")
    List<Tuple> loadEventDataByIds(List<Long> ids);

    /**
     * Retrieves a list of event preview data for the given event IDs and user ID
     * for unauthorized user. Does not support order from entry list.
     *
     * @param ids    A list of event IDs for which the preview data is to be
     *               retrieved.
     * @param userId The ID of the user for which the event preview data is being
     *               retrieved.
     * @return A list of Tuples containing the event preview data for the specified
     *         IDs.
     */
    @Query(nativeQuery = true,
        value = """
            SELECT e.id AS eventId, e.title, e.description, et.tag_id AS tagId, l.code AS languageCode,
                   tt.name AS tagName, e.is_open, u.id AS organizerId, u.name AS organizerName,
                   e.title_image, e.creation_date, start_date, finish_date, online_link,
                   latitude, longitude, street_en, street_ua, house_number, city_en, city_ua,
                   region_en, region_ua, country_en, country_ua, formatted_address_en,
                   formatted_address_ua, e.type,
                   (CURRENT_DATE <= edl_max.latest_finish_date) AS isRelevant,
                   COUNT(DISTINCT eul)                               AS likes,
                   COUNT(DISTINCT eud)                               AS dislikes,
                   COUNT(DISTINCT ec)                                AS countComments,
                   AVG(eg.grade)                                     AS grade,
                   (uf.friend_id IS NOT NULL)                        AS isOrganizedByFriend,
                   (e.organizer_id = :userId)                        AS isOrganizedByUser,
                   (ea.user_id = :userId AND ea.user_id IS NOT NULL) AS isSubscribed,
                   (ef.user_id IS NOT NULL)                          AS isFavorite,
                   CASE
                        WHEN eg.user_id = :userId THEN eg.grade
                        ELSE NULL
                    END AS currentUserGrade
            FROM events e
                LEFT JOIN events_grades eg ON e.id = eg.event_id
                LEFT JOIN comments ec ON e.id = ec.article_id AND ec.article_type = 'EVENT' AND ec.status != 'DELETED'
                LEFT JOIN events_users_likes eul ON e.id = eul.event_id
                LEFT JOIN events_users_dislikes eud ON e.id = eud.event_id
                LEFT JOIN events_dates_locations edl ON e.id = edl.event_id
                LEFT JOIN (
                   SELECT event_id, MAX(finish_date) AS latest_finish_date
                   FROM events_dates_locations
                   GROUP BY event_id
                ) edl_max ON e.id = edl_max.event_id
                     LEFT JOIN events_tags et ON e.id = et.event_id
                     LEFT JOIN tag_translations tt ON et.tag_id = tt.tag_id
                     LEFT JOIN languages l ON tt.language_id = l.id
                     LEFT JOIN users u ON e.organizer_id = u.id
                     LEFT JOIN users_friends uf ON
                         uf.user_id = :userId AND uf.friend_id=e.organizer_id AND uf.status='FRIEND'
                     LEFT JOIN events_followers ef ON e.id = ef.event_id AND ef.user_id = :userId
                     LEFT JOIN events_attenders ea ON e.id = ea.event_id
            WHERE (e.id IN (:ids))
                GROUP BY e.id, tt.name, edl.city_en, et.tag_id, l.code, u.id, edl.id,eg.user_id,
                                uf.friend_id, ea.user_id, ef.user_id, edl_max.latest_finish_date,eg.grade;
            """)
    List<Tuple> loadEventDataByIds(List<Long> ids, Long userId);

    /**
     * Method returns all images of events by event id.
     *
     * @param eventId {@link Long} event id.
     * @return List of {@link String} links of event images.
     * @author Olena Sotnik
     */
    @Query(nativeQuery = true,
        value = "SELECT eim.link FROM events AS e "
            + "JOIN events_images AS eim "
            + "ON e.id = eim.event_id "
            + "WHERE e.id = :eventId")
    List<String> findAllImagesLinksByEventId(Long eventId);
}
