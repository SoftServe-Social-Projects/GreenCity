package greencity.mapping;

import greencity.dto.search.SearchTipsAndTricksDto;
import greencity.dto.user.AuthorDto;
import greencity.entity.TipsAndTricks;
import greencity.entity.User;
import greencity.entity.localization.TagTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
public class SearchTipsAndTricksDtoMapper extends AbstractConverter<TipsAndTricks, SearchTipsAndTricksDto> {
    @Override
    protected SearchTipsAndTricksDto convert(TipsAndTricks tipsAndTricks) {
        User author = tipsAndTricks.getAuthor();
        String language = LocaleContextHolder.getLocale().getLanguage();

        return SearchTipsAndTricksDto.builder()
            .id(tipsAndTricks.getId())
            .title(tipsAndTricks.getTitleTranslations()
                .stream()
                .filter(elem -> elem.getLanguage().getCode().equals(language))
                .findFirst().orElseThrow(RuntimeException::new).getContent())
            .author(new AuthorDto(author.getId(),
                author.getName()))
            .creationDate(tipsAndTricks.getCreationDate())
            .tags(tipsAndTricks.getTags().stream().flatMap(t -> t.getTagTranslations().stream())
                .filter(tagTranslation -> tagTranslation.getLanguage().getCode().equals(language))
                .map(TagTranslation::getName).collect(Collectors.toList()))
            .build();
    }
}
