package greencity.mapping;

import greencity.dto.tag.TagVO;
import greencity.entity.Language;
import greencity.entity.Tag;
import greencity.entity.localization.TagTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class TagMapper extends AbstractConverter<TagVO, Tag> {
    @Override
    protected Tag convert(TagVO tagVO) {
        Tag tag = new Tag();
        tag.setId(tagVO.getId());
        tag.setType(tagVO.getType());
        List<TagTranslation> tagTranslations = new ArrayList<>();
        tagVO.getTagTranslations()
            .forEach(tt -> tagTranslations.add(TagTranslation.builder().id(tt.getId()).name(tt.getName())
                .language(Language.builder().code(tt.getLanguageVO().getCode()).id(tt.getLanguageVO().getId()).build())
                .build()));
        tag.setTagTranslations(tagTranslations);
        return tag;
    }
}
