package greencity.dto.placecomment;

import greencity.enums.SortableFields;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class PlaceCommentAdminDtoTest {

    @Test
    void getSortableFields() {
        PlaceCommentAdminDto placeCommentAdminDto = new PlaceCommentAdminDto();
        List<String> sortableFields = placeCommentAdminDto.getSortableFields();

        assertTrue(sortableFields.contains(SortableFields.ID.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.CREATED_DATE.getFieldName()));
    }
}