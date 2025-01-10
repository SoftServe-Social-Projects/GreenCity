package greencity.dto.user;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class SubscriberDto {
    private String name;
    private String email;
    private String language;
    private UUID unsubscribeToken;
}
