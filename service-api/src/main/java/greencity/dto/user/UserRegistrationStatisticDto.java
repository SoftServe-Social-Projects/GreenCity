package greencity.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class UserRegistrationStatisticDto {
    private LocalDateTime date;
    private Long count;
}