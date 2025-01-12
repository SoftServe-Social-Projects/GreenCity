package greencity.dto.habittranslation;

import java.io.Serializable;
import jakarta.validation.constraints.NotBlank;
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
public class HabitTranslationDto implements Serializable {
    @NotBlank
    private String description;
    private String habitItem;
    @NotBlank
    private String languageCode;
    @NotBlank
    private String name;
    private String descriptionUa;
    private String nameUa;
    private String habitItemUa;
}
