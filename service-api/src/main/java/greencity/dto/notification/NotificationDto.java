package greencity.dto.notification;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.experimental.SuperBuilder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@SuperBuilder
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
