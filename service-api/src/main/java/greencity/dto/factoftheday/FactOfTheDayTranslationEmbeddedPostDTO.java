package greencity.dto.factoftheday;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactOfTheDayTranslationEmbeddedPostDTO {
    @Size(min = 0, max = 300)
    private String content;
    @NotNull
    private String languageCode;
}
