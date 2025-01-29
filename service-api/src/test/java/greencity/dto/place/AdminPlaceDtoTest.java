package greencity.dto.place;

import greencity.enums.SortableFields;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminPlaceDtoTest {
    @Test
    void getSortableFieldsShouldReturnExpectedFieldsTest() {
        AdminPlaceDto adminPlaceDto = new AdminPlaceDto();
        List<String> sortableFields = adminPlaceDto.getSortableFields();

        assertTrue(sortableFields.contains(SortableFields.ID.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.NAME.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.MODIFIED_DATE.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.IS_FAVORITE.getFieldName()));
    }
}
