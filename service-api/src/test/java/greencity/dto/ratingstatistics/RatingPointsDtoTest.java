package greencity.dto.ratingstatistics;

import greencity.enums.SortableFields;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RatingPointsDtoTest {

    @Test
    void getSortableFields() {
        RatingPointsDto ratingPointsDto = new RatingPointsDto();
        List<String> sortableFields = ratingPointsDto.getSortableFields();
        Set<String> expectedFields = Set.of(
            SortableFields.ID.getFieldName(),
            SortableFields.NAME.getFieldName(),
            SortableFields.POINTS.getFieldName());

        assertEquals(expectedFields, new HashSet<>(sortableFields));
    }
}