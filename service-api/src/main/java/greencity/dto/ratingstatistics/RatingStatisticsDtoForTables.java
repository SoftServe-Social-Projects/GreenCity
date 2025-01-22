package greencity.dto.ratingstatistics;

import greencity.dto.Sortable;
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

    @Override
    public List<String> getSortableFields() {
        return List.of();
    }
}
