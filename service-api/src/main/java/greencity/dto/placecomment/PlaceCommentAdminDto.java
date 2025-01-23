package greencity.dto.placecomment;

import greencity.dto.Sortable;
import greencity.dto.photo.PhotoReturnDto;
import greencity.dto.place.AdminPlaceDto;
import greencity.enums.SortableFields;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceCommentAdminDto implements Sortable {
    private Long id;
    private String text;
    private LocalDateTime createdDate;
    private List<PhotoReturnDto> photos;
    private AdminPlaceDto place;

    @Override
    public List<String> getSortableFields() {
        return List.of(
            SortableFields.ID.getFieldName(),
            SortableFields.CREATED_DATE.getFieldName());
    }
}
