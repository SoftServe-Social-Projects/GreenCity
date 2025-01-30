package greencity.dto.ratingstatistics;

import greencity.enums.SortableFields;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RatingStatisticsDtoForTablesTest {

    @Test
    void getSortableFields() {
        RatingStatisticsDtoForTables dto = new RatingStatisticsDtoForTables();
        List<String> sortableFields = dto.getSortableFields();
        Set<String> expectedFields = Set.of(
            SortableFields.ID.getFieldName(),
            SortableFields.CREATE_DATE.getFieldName(),
            SortableFields.EVENT_NAME.getFieldName(),
            SortableFields.RATING.getFieldName());

        assertEquals(expectedFields, new HashSet<>(sortableFields));
    }
}