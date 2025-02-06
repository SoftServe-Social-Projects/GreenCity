package greencity.dto.factoftheday;

import greencity.enums.SortableFields;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FactOfTheDayDTOTest {

    @Test
    void getSortableFields() {
        FactOfTheDayDTO factOfTheDayDTO = new FactOfTheDayDTO();
        List<String> sortableFields = factOfTheDayDTO.getSortableFields();
        Set<String> expectedFields = Set.of(
            SortableFields.ID.getFieldName(),
            SortableFields.NAME.getFieldName(),
            SortableFields.CREATE_DATE.getFieldName());

        assertEquals(expectedFields, new HashSet<>(sortableFields));
    }
}