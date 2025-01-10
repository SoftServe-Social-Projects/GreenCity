package greencity.dto.habittranslation;

import java.io.Serializable;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class HabitTranslationManagementDto implements Serializable {
    private Long id;
    @NotEmpty
    @Size(min = 1, max = 170)
    private String name;
    @NotEmpty
    @Size(min = 1, max = 170)
    private String habitItem;
    @NotEmpty
    @Size(min = 1, max = 170)
    private String description;
    @NotEmpty
    private String languageCode;
}
