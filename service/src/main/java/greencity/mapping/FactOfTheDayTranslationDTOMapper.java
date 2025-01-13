package greencity.mapping;

import greencity.dto.factoftheday.FactOfTheDayTranslationDTO;
import greencity.dto.factoftheday.FactOfTheDayTranslationEmbeddedPostDTO;
import greencity.entity.FactOfTheDay;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class FactOfTheDayTranslationDTOMapper extends AbstractConverter<FactOfTheDay, FactOfTheDayTranslationDTO> {
    @Override
    protected FactOfTheDayTranslationDTO convert(FactOfTheDay factOfTheDay) {
        return FactOfTheDayTranslationDTO.builder()
            .id(factOfTheDay.getId())
            .factOfTheDayTranslations(factOfTheDay.getFactOfTheDayTranslations().stream()
                .map(factOfTheDayTranslation -> FactOfTheDayTranslationEmbeddedPostDTO.builder()
                    .content(factOfTheDayTranslation.getContent())
                    .languageCode(factOfTheDayTranslation.getLanguage().getCode())
                    .build())
                .toList())
            .build();
    }
}
