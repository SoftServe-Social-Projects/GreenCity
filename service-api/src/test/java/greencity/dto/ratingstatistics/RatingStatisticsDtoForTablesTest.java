package greencity.dto.ratingstatistics;

import greencity.enums.SortableFields;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RatingStatisticsDtoForTablesTest {

    @Test
    void getSortableFields() {
        RatingStatisticsDtoForTables dto = new RatingStatisticsDtoForTables();
        List<String> sortableFields = dto.getSortableFields();

        assertTrue(sortableFields.contains(SortableFields.ID.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.CREATE_DATE.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.EVENT_NAME.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.RATING.getFieldName()));
    }
}