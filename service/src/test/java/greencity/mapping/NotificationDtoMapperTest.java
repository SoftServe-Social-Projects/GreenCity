package greencity.mapping;

import greencity.dto.notification.NotificationDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import static greencity.ModelUtils.getNotification;
import static greencity.ModelUtils.getNotificationDto;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class NotificationDtoMapperTest {
    @InjectMocks
    private NotificationDtoMapper notificationDtoMapper;

    @Test
    void convertTest() {
        var dto = getNotificationDto();
        var notification = getNotification();
        NotificationDto convert = notificationDtoMapper.convert(notification);
        assertEquals(dto.getMessage(), convert.getMessage());
    }
}
