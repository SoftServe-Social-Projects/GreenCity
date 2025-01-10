package greencity.dto.tag;

import greencity.dto.econews.EcoNewsVO;
import greencity.dto.event.EventVO;
import greencity.dto.habit.HabitVO;
import java.util.List;
import java.util.Set;
import greencity.enums.TagType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class TagVO {
    private Long id;
    private TagType type;
    private List<TagTranslationVO> tagTranslations;
    private List<EcoNewsVO> ecoNews;
    private Set<HabitVO> habits;
    private Set<EventVO> events;
}
