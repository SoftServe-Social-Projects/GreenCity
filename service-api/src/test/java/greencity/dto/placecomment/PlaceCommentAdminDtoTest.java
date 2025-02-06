package greencity.dto.placecomment;

import greencity.enums.SortableFields;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceCommentAdminDtoTest {

    @Test
    void getSortableFields() {
        PlaceCommentAdminDto placeCommentAdminDto = new PlaceCommentAdminDto();
        List<String> sortableFields = placeCommentAdminDto.getSortableFields();
        Set<String> expectedFields = Set.of(
            SortableFields.ID.getFieldName(),
            SortableFields.CREATED_DATE.getFieldName());

        assertEquals(expectedFields, new HashSet<>(sortableFields));
    }
}