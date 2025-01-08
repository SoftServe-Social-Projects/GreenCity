package greencity.dto.notification;

import greencity.dto.user.UserVO;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class EmailNotificationDto {
    private Long id;

    private UserVO targetUser;

    private List<UserVO> actionUsers = new ArrayList<>();

    private String customMessage;

    private Long targetId;

    private String secondMessage;

    private Long secondMessageId;

    private NotificationType notificationType;

    private ProjectName projectName;

    private boolean viewed;

    private ZonedDateTime time;

    private boolean emailSent;
}
