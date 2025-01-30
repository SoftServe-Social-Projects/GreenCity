package greencity.dto.habit;

import greencity.enums.SortableFields;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class HabitDtoTest {

    @Test
    void getSortableFields() {
        HabitDto habitDto = new HabitDto();
        List<String> sortableFields = habitDto.getSortableFields();
        Set<String> expectedFields = Set.of(
            SortableFields.ID.getFieldName(),
            SortableFields.DEFAULT_DURATION.getFieldName(),
            SortableFields.AMOUNT_ACQUIRED_USERS.getFieldName(),
            SortableFields.COMPLEXITY.getFieldName(),
            SortableFields.LIKES.getFieldName(),
            SortableFields.DISLIKES.getFieldName());

        assertEquals(expectedFields, new HashSet<>(sortableFields));
    }
}