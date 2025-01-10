package greencity.mapping;

import java.util.Optional;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import greencity.dto.tag.NewTagDto;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;

@Component
public class NewTagDtoMapper extends AbstractConverter<Tag, NewTagDto> {
    @Override
    protected NewTagDto convert(Tag source) {
        Optional<TagTranslation> tagTranslationEn = source.getTagTranslations().stream()
            .filter(tagTranslation -> tagTranslation.getLanguage().getCode().equals("en"))
            .findFirst();
        Optional<TagTranslation> tagTranslationUa = source.getTagTranslations().stream()
            .filter(tagTranslation -> tagTranslation.getLanguage().getCode().equals("ua"))
            .findFirst();
        String name = tagTranslationEn.map(TagTranslation::getName).orElse(null);
        String nameUa = tagTranslationUa.map(TagTranslation::getName).orElse(null);

        return NewTagDto.builder()
            .name(name)
            .nameUa(nameUa)
            .id(source.getId())
            .build();
    }
}