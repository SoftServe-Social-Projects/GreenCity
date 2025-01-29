package greencity.dto.factoftheday;

import greencity.enums.SortableFields;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class FactOfTheDayDTOTest {

    @Test
    void getSortableFields() {
        FactOfTheDayDTO factOfTheDayDTO = new FactOfTheDayDTO();
        List<String> sortableFields = factOfTheDayDTO.getSortableFields();

        assertTrue(sortableFields.contains(SortableFields.ID.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.NAME.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.CREATE_DATE.getFieldName()));
    }
}