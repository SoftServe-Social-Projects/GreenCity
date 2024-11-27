package greencity.dto.notification;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.ZonedDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class NotificationDto {
    private Long notificationId;
    private String projectName;
    private String notificationType;
    private ZonedDateTime time;
    private Boolean viewed;

    private String titleText;
    private String bodyText;
    private List<Long> actionUserId;
    private List<String> actionUserText;
    private Long targetId;
    private String message;
    private String secondMessage;
    private Long secondMessageId;
}
