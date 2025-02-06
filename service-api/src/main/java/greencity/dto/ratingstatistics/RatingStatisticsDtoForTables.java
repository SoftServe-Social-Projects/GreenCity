package greencity.dto.ratingstatistics;

import greencity.dto.Sortable;
import greencity.enums.SortableFields;
import java.time.ZonedDateTime;
import java.util.List;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class RatingStatisticsDtoForTables implements Sortable {
    private Long id;
    private ZonedDateTime createDate;
    private String eventName;
    private float pointsChanged;
    private float rating;
    private long userId;
    private String userEmail;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSortableFields() {
        return List.of(
            SortableFields.ID.getFieldName(),
            SortableFields.CREATE_DATE.getFieldName(),
            SortableFields.EVENT_NAME.getFieldName(),
            SortableFields.RATING.getFieldName());
    }
}
