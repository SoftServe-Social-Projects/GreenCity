package greencity.dto.tag;

import greencity.dto.language.LanguageVO;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class TagTranslationVO {
    private Long id;
    private String name;
    private LanguageVO languageVO;
}
