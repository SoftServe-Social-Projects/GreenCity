package greencity.dto.econews;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EcoNewsAuthorStatisticDto {
    Long number;
    Long id;
    String name;
    Long ecoNewsPostsCount;
}
