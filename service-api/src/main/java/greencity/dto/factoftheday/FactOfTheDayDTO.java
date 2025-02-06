package greencity.dto.factoftheday;

import greencity.dto.Sortable;
import greencity.enums.SortableFields;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Set;
import greencity.dto.tag.TagDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FactOfTheDayDTO implements Sortable {
    private Long id;
    private String name;
    private List<FactOfTheDayTranslationEmbeddedDTO> factOfTheDayTranslations;
    private ZonedDateTime createDate;
    private Set<TagDto> tags;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSortableFields() {
        return List.of(
            SortableFields.ID.getFieldName(),
            SortableFields.NAME.getFieldName(),
            SortableFields.CREATE_DATE.getFieldName());
    }
}
