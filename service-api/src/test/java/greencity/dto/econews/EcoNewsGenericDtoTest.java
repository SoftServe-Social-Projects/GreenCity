package greencity.dto.econews;

import greencity.enums.SortableFields;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EcoNewsGenericDtoTest {

    @Test
    void getSortableFields() {
        EcoNewsGenericDto ecoNewsGenericDto = new EcoNewsGenericDto();
        List<String> sortableFields = ecoNewsGenericDto.getSortableFields();
        Set<String> expectedFields = Set.of(
            SortableFields.ID.getFieldName(),
            SortableFields.TITLE.getFieldName(),
            SortableFields.CREATION_DATE.getFieldName(),
            SortableFields.LIKES.getFieldName(),
            SortableFields.COUNT_COMMENTS.getFieldName(),
            SortableFields.COUNT_OF_ECO_NEWS.getFieldName());

        assertEquals(expectedFields, new HashSet<>(sortableFields));
    }
}