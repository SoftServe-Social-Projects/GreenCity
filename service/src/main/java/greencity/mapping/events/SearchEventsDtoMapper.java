package greencity.mapping.events;

import greencity.dto.search.SearchEventsDto;
import greencity.entity.event.Event;
import greencity.entity.localization.TagTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SearchEventsDtoMapper extends AbstractConverter<Event, SearchEventsDto> {
    @Override
    protected SearchEventsDto convert(Event event) {
        String language = LocaleContextHolder.getLocale().getLanguage();
        return SearchEventsDto.builder()
            .id(event.getId())
            .title(event.getTitle())
            .tags(event.getTags().stream()
                .flatMap(t -> t.getTagTranslations().stream())
                .filter(tagTranslation -> tagTranslation.getLanguage().getCode().equals(language))
                .map(TagTranslation::getName)
                .toList())
            .build();
    }
}
