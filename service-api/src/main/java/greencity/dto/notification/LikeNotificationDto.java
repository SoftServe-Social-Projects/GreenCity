package greencity.dto.notification;

import greencity.dto.user.UserVO;
import greencity.enums.NotificationType;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class LikeNotificationDto {
    private UserVO targetUserVO;
    private UserVO actionUserVO;
    private Long newsId;
    private String newsTitle;
    private NotificationType notificationType;
    private boolean isLike;
    private Long secondMessageId;
    private String secondMessageText;
}
