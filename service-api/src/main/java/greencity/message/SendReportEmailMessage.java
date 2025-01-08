package greencity.message;

import greencity.dto.category.CategoryDto;
import greencity.dto.place.PlaceNotificationDto;
import greencity.dto.user.SubscriberDto;
import greencity.enums.EmailPreferencePeriodicity;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SendReportEmailMessage {
    private List<SubscriberDto> subscribers;
    private Map<CategoryDto, List<PlaceNotificationDto>> categoriesDtoWithPlacesDtoMap;
    private EmailPreferencePeriodicity periodicity;
}
