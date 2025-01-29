package greencity.dto.econews;

import greencity.enums.SortableFields;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class EcoNewsGenericDtoTest {

    @Test
    void getSortableFields() {
        EcoNewsGenericDto ecoNewsGenericDto = new EcoNewsGenericDto();
        List<String> sortableFields = ecoNewsGenericDto.getSortableFields();

        assertTrue(sortableFields.contains(SortableFields.TITLE.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.ID.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.CREATION_DATE.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.ID.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.TITLE.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.CREATION_DATE.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.LIKES.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.COUNT_COMMENTS.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.COUNT_OF_ECO_NEWS.getFieldName()));
    }
}