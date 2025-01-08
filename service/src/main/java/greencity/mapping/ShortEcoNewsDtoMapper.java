package greencity.mapping;

import greencity.dto.econews.EcoNewsDto;
import greencity.dto.econews.ShortEcoNewsDto;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class ShortEcoNewsDtoMapper extends AbstractConverter<EcoNewsDto, ShortEcoNewsDto> {
    @Override
    protected ShortEcoNewsDto convert(EcoNewsDto ecoNews) {
        return ShortEcoNewsDto.builder()
            .ecoNewsId(ecoNews.getId())
            .imagePath(ecoNews.getImagePath())
            .title(ecoNews.getTitle())
            .text(ecoNews.getContent())
            .build();
    }
}
