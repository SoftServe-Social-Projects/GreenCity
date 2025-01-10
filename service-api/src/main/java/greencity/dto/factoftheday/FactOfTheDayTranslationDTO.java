package greencity.dto.factoftheday;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactOfTheDayTranslationDTO {
    private Long id;

    private List<FactOfTheDayTranslationEmbeddedPostDTO> factOfTheDayTranslations;
}
