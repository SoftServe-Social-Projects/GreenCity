package greencity.dto.habit;

import greencity.enums.SortableFields;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class HabitDtoTest {

    @Test
    void getSortableFields() {
        HabitDto habitDto = new HabitDto();
        List<String> sortableFields = habitDto.getSortableFields();

        assertTrue(sortableFields.contains(SortableFields.ID.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.DEFAULT_DURATION.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.AMOUNT_ACQUIRED_USERS.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.COMPLEXITY.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.LIKES.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.DISLIKES.getFieldName()));
    }
}