package greencity.dto.user;

import lombok.Value;

@Value
public class RegistrationStatisticsDtoResponse {
    Integer month;
    Long count;
}
