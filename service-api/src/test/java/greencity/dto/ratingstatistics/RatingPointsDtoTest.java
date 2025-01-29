package greencity.dto.ratingstatistics;

import greencity.enums.SortableFields;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class RatingPointsDtoTest {

    @Test
    void getSortableFields() {
        RatingPointsDto ratingPointsDto = new RatingPointsDto();
        List<String> sortableFields = ratingPointsDto.getSortableFields();

        assertTrue(sortableFields.contains(SortableFields.ID.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.NAME.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.POINTS.getFieldName()));
    }
}