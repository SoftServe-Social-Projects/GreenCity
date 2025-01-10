package greencity.mapping;

import greencity.dto.econews.AddEcoNewsDtoResponse;
import greencity.dto.user.EcoNewsAuthorDto;
import greencity.entity.EcoNews;
import java.util.stream.Collectors;
import greencity.entity.localization.TagTranslation;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link EcoNews} into
 * {@link AddEcoNewsDtoResponse}.
 */
@Component
public class AddEcoNewsDtoResponseMapper extends AbstractConverter<EcoNews, AddEcoNewsDtoResponse> {
    /**
     * Method for converting {@link EcoNews} into {@link AddEcoNewsDtoResponse}.
     *
     * @param ecoNews object to convert.
     * @return converted object.
     */
    @Override
    protected AddEcoNewsDtoResponse convert(EcoNews ecoNews) {
        return AddEcoNewsDtoResponse.builder()
            .id(ecoNews.getId())
            .text(ecoNews.getText())
            .title(ecoNews.getTitle())
            .source(ecoNews.getSource())
            .imagePath(ecoNews.getImagePath())
            .creationDate(ecoNews.getCreationDate())
            .shortInfo(ecoNews.getShortInfo())
            .ecoNewsAuthorDto(EcoNewsAuthorDto.builder()
                .id(ecoNews.getAuthor().getId())
                .name(ecoNews.getAuthor().getName())
                .build())
            .tags(ecoNews.getTags().stream().flatMap(t -> t.getTagTranslations().stream())
                .map(TagTranslation::getName).collect(Collectors.toList()))
            .build();
    }
}
