package greencity.dto.comment;

import greencity.enums.SortableFields;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class CommentDtoTest {
    @Test
    void getSortableFieldsTest() {
        CommentDto commentDto = new CommentDto();
        List<String> sortableFields = commentDto.getSortableFields();

        assertTrue(sortableFields.contains(SortableFields.ID.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.CREATED_DATE.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.CREATED_DATE.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.MODIFIED_DATE.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.REPLIES.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.LIKES.getFieldName()));
        assertTrue(sortableFields.contains(SortableFields.DISLIKES.getFieldName()));
    }
}