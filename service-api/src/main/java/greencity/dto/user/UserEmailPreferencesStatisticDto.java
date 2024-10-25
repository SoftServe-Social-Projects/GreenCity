package greencity.dto.user;

import greencity.enums.EmailPreference;
import greencity.enums.EmailPreferencePeriodicity;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserEmailPreferencesStatisticDto {
    private EmailPreference emailPreference;
    private EmailPreferencePeriodicity periodicity;
    private Long count;
}
