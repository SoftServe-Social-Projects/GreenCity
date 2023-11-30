package greencity.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class NotificationDtoResponse {
    private Long amountOfNotificationsAcquired;
    private List<SingleNotificationDto> notificationDtoList;
}
