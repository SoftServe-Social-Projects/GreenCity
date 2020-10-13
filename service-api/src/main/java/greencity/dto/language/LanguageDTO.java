package greencity.dto.language;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(of = {"id", "code"})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LanguageDTO {
    @Min(1)
    private Long id;

    @NotNull
    private String code;
}
