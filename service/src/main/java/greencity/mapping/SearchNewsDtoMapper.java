package greencity.mapping;

import greencity.dto.search.SearchNewsDto;
import greencity.entity.EcoNews;
import greencity.entity.localization.TagTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SearchNewsDtoMapper extends AbstractConverter<EcoNews, SearchNewsDto> {
    @Override
    protected SearchNewsDto convert(EcoNews ecoNews) {
        String language = LocaleContextHolder.getLocale().getLanguage();
        return SearchNewsDto.builder()
            .id(ecoNews.getId())
            .title(ecoNews.getTitle())
            .tags(ecoNews.getTags().stream()
                .flatMap(t -> t.getTagTranslations().stream())
                .filter(tagTranslation -> tagTranslation.getLanguage().getCode().equals(language))
                .map(TagTranslation::getName)
                .toList())
            .build();
    }
}
