package greencity.dto.language;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.*;

@EqualsAndHashCode(of = {"id", "code"})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LanguageDTO {
    @Min(1)
    private Long id;

    @NotNull
    private String code;
}
