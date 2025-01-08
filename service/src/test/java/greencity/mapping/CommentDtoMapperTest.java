package greencity.mapping;

import greencity.dto.comment.CommentDto;
import greencity.entity.Comment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.List;
import static greencity.ModelUtils.getComment;
import static greencity.ModelUtils.getCommentImage;
import static greencity.ModelUtils.getParentComment;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class CommentDtoMapperTest {

    @InjectMocks
    CommentDtoMapper mapper;

    @Test
    void convertToDto() {
        Comment comment = getComment();
        CommentDto commentDto = mapper.convert(comment);

        assertEquals(comment.getId(), commentDto.getId());
        assertEquals(comment.getText(), commentDto.getText());
        assertEquals(comment.getCreatedDate(), commentDto.getCreatedDate());
        assertEquals(comment.getModifiedDate(), commentDto.getModifiedDate());
        assertNull(commentDto.getParentCommentId());
    }

    @Test
    void convertToDtoWithImages() {
        Comment comment = getComment();
        comment.setParentComment(getParentComment());
        comment.setAdditionalImages(List.of(getCommentImage()));
        CommentDto commentDto = mapper.convert(comment);

        assertEquals(comment.getId(), commentDto.getId());
        assertEquals(comment.getText(), commentDto.getText());
        assert commentDto.getAdditionalImages() != null;
        assertEquals(comment.getAdditionalImages().getFirst().getLink(), commentDto.getAdditionalImages().getFirst());
        assertEquals(comment.getCreatedDate(), commentDto.getCreatedDate());
        assertEquals(comment.getModifiedDate(), commentDto.getModifiedDate());
        assertEquals(comment.getParentComment().getId(), commentDto.getParentCommentId());
    }
}
