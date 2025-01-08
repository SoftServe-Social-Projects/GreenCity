package greencity.mapping;

import greencity.dto.factoftheday.FactOfTheDayDTO;
import greencity.dto.factoftheday.FactOfTheDayTranslationEmbeddedDTO;
import greencity.dto.language.LanguageDTO;
import greencity.entity.FactOfTheDay;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class FactOfTheDayDtoMapper extends AbstractConverter<FactOfTheDay, FactOfTheDayDTO> {
    @Override
    protected FactOfTheDayDTO convert(FactOfTheDay factOfTheDay) {
        return FactOfTheDayDTO.builder()
            .id(factOfTheDay.getId())
            .name(factOfTheDay.getName())
            .factOfTheDayTranslations(factOfTheDay.getFactOfTheDayTranslations().stream()
                .map(factOfTheDayTranslation -> FactOfTheDayTranslationEmbeddedDTO.builder()
                    .id(factOfTheDayTranslation.getId())
                    .content(factOfTheDayTranslation.getContent())
                    .language(LanguageDTO.builder()
                        .id(factOfTheDayTranslation.getLanguage().getId())
                        .code(factOfTheDayTranslation.getLanguage().getCode())
                        .build())
                    .build())
                .toList())
            .createDate(factOfTheDay.getCreateDate())
            .build();
    }
}
