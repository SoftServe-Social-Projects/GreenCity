package greencity.dto.tag;

import greencity.annotations.LanguageTranslationConstraint;
import greencity.enums.TagType;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class TagPostDto {
    @NotNull
    private TagType type;

    @Valid
    @LanguageTranslationConstraint
    private List<TagTranslationDto> tagTranslations;
}
