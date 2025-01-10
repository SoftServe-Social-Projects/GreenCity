package greencity.mapping;

import greencity.dto.language.LanguageVO;
import greencity.dto.tag.TagTranslationVO;
import greencity.dto.tag.TagVO;
import greencity.entity.Tag;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

@Component
public class TagVOMapper extends AbstractConverter<Tag, TagVO> {
    @Override
    protected TagVO convert(Tag tag) {
        return TagVO.builder()
            .id(tag.getId())
            .type(tag.getType())
            .tagTranslations(tag.getTagTranslations().stream()
                .map(tagTranslation -> TagTranslationVO.builder()
                    .id(tagTranslation.getId())
                    .name(tagTranslation.getName())
                    .languageVO(LanguageVO.builder()
                        .id(tagTranslation.getLanguage().getId())
                        .code(tagTranslation.getLanguage().getCode())
                        .build())
                    .build())
                .collect(Collectors.toList()))
            .build();
    }
}
