package greencity.dto.user;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserLocationStatisticDto {
    private String location;
    private Long count;
}
