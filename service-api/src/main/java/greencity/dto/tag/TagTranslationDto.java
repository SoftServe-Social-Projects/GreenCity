package greencity.dto.tag;

import greencity.dto.language.LanguageTranslationDTO;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;
import lombok.experimental.SuperBuilder;

@Getter
@Setter
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TagTranslationDto extends LanguageTranslationDTO {
    @Valid
    @Size(min = 1, max = 20)
    private String name;
}
