package greencity.dto.tag;

import greencity.constant.ServiceValidationConstants;
import greencity.dto.language.LanguageDTO;
import greencity.dto.language.LanguageTranslationDTO;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

@Getter
@Setter
@EqualsAndHashCode
public class TagTranslationDto extends LanguageTranslationDTO {
    @Valid
    @Size(min = 1, max = 20)
    private String name;

    /**
     * Constructor.
     */
    @Builder(builderMethodName = "TagTranslationDtoBuilder")
    public TagTranslationDto(
        @Valid @NotNull LanguageDTO language,
        @Size(min = ServiceValidationConstants.ADVICE_MIN_LENGTH,
            max = ServiceValidationConstants.ADVICE_MAX_LENGTH) String content,
        @Valid @Size(min = 1, max = 20) String name) {
        super(language, content);
        this.name = name;
    }
}
