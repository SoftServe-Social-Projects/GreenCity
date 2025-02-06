package greencity.dto.comment;

import greencity.enums.SortableFields;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommentDtoTest {
    @Test
    void getSortableFieldsTest() {
        CommentDto commentDto = new CommentDto();
        List<String> sortableFields = commentDto.getSortableFields();
        Set<String> expectedFields = Set.of(
            SortableFields.ID.getFieldName(),
            SortableFields.CREATED_DATE.getFieldName(),
            SortableFields.MODIFIED_DATE.getFieldName(),
            SortableFields.REPLIES.getFieldName(),
            SortableFields.LIKES.getFieldName(),
            SortableFields.DISLIKES.getFieldName());

        assertEquals(expectedFields, new HashSet<>(sortableFields));
    }
}