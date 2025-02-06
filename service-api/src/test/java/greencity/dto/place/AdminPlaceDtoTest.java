package greencity.dto.place;

import greencity.enums.SortableFields;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AdminPlaceDtoTest {
    @Test
    void getSortableFieldsShouldReturnExpectedFieldsTest() {
        AdminPlaceDto adminPlaceDto = new AdminPlaceDto();
        List<String> sortableFields = adminPlaceDto.getSortableFields();
        Set<String> expectedFields = Set.of(
            SortableFields.ID.getFieldName(),
            SortableFields.NAME.getFieldName(),
            SortableFields.MODIFIED_DATE.getFieldName(),
            SortableFields.IS_FAVORITE.getFieldName());

        assertEquals(expectedFields, new HashSet<>(sortableFields));
    }
}
