package greencity.dto.habit;

import greencity.enums.SortableFields;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HabitManagementDtoTest {

    @Test
    void getSortableFields() {
        HabitManagementDto dto = new HabitManagementDto();
        List<String> sortableFields = dto.getSortableFields();

        assertTrue(sortableFields.contains(SortableFields.ID.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.COMPLEXITY.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.DEFAULT_DURATION.getFieldName()));

    }
}