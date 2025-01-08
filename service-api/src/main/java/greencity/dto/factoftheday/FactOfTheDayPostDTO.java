package greencity.dto.factoftheday;

import java.util.List;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FactOfTheDayPostDTO {
    @NotNull
    private Long id;
    @Size(min = 1, max = 300)
    private String name;
    @Valid
    private List<FactOfTheDayTranslationEmbeddedPostDTO> factOfTheDayTranslations;
    private List<Long> tags;
}
