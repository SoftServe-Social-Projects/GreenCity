package greencity.mapping;

import greencity.dto.search.SearchPlacesDto;
import greencity.entity.Place;
import org.modelmapper.AbstractConverter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SearchPlacesDtoMapper extends AbstractConverter<Place, SearchPlacesDto> {
    @Override
    protected SearchPlacesDto convert(Place place) {
        String language = LocaleContextHolder.getLocale().getLanguage();
        return SearchPlacesDto.builder()
            .id(place.getId())
            .name(place.getName())
            .category(language.equals("ua") ? place.getCategory().getNameUa() : place.getCategory().getName())
            .build();
    }
}
