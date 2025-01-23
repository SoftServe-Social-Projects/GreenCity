package greencity.dto.ratingstatistics;

import greencity.dto.Sortable;
import greencity.enums.SortableFields;
import greencity.enums.Status;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Builder
public class RatingPointsDto implements Sortable {
    private Long id;
    private String name;
    private Integer points;
    private Status status;

    @Override
    public List<String> getSortableFields() {
        return List.of(
            SortableFields.ID.getFieldName(),
            SortableFields.NAME.getFieldName(),
            SortableFields.POINTS.getFieldName());
    }
}
