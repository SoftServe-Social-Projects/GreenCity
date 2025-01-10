package greencity.dto.factoftheday;

import greencity.dto.translation.TranslationVO;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@SuperBuilder
@NoArgsConstructor
@EqualsAndHashCode(callSuper = true)
public class FactOfTheDayTranslationVO extends TranslationVO {
    private FactOfTheDayVO factOfTheDay;
}
