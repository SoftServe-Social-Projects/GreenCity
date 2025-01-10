package greencity.repository;

import greencity.entity.Notification;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CustomNotificationRepo {
    /**
     * Method for finding notification by filter.
     *
     * @param targetUserId      user, which should return notification.
     * @param projectName       project name.
     * @param notificationTypes notifications type.
     * @param viewed            true or false. Can be null.
     * @param pageable          pageable.
     * @return page of {@link Notification}
     */
    Page<Notification> findNotificationsByFilter(Long targetUserId, ProjectName projectName,
        List<NotificationType> notificationTypes, Boolean viewed, Pageable pageable);
}
