package greencity.dto.factoftheday;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactOfTheDayTranslationEmbeddedPostDTO {
    @Size(max = 300)
    private String content;
    @NotNull
    private String languageCode;
}
