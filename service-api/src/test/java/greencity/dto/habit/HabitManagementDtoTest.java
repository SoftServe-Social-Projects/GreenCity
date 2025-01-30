package greencity.dto.habit;

import greencity.enums.SortableFields;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HabitManagementDtoTest {

    @Test
    void getSortableFields() {
        HabitManagementDto dto = new HabitManagementDto();
        List<String> sortableFields = dto.getSortableFields();
        Set<String> expectedFields = Set.of(
            SortableFields.ID.getFieldName(),
            SortableFields.COMPLEXITY.getFieldName(),
            SortableFields.DEFAULT_DURATION.getFieldName());

        assertEquals(expectedFields, new HashSet<>(sortableFields));
    }
}