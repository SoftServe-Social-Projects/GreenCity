package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.comment.CommentDto;
import greencity.entity.Comment;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@ExtendWith(MockitoExtension.class)
class EventCommentDtoMapperTest {
    @InjectMocks
    private CommentDtoMapper commentDtoMapper;

    @Test
    void convertTest() {
        Comment comment = ModelUtils.getComment().setParentComment(new Comment().setId(2L));
        CommentDto actual = commentDtoMapper.convert(comment);

        assertNotNull(actual);
        assertEquals(comment.getId(), actual.getId(), "Comment ID mismatch");
        assertEquals(comment.getText(), actual.getText(), "Comment text mismatch");
        assertEquals(comment.getModifiedDate(), actual.getModifiedDate(), "Modified date mismatch");
        assertEquals(comment.getUsersLiked().size(), actual.getLikes(), "Likes count mismatch");
        assertEquals(comment.isCurrentUserLiked(), actual.isCurrentUserLiked(), "Current user liked status mismatch");
        assertEquals(comment.getParentComment().getId(), actual.getParentCommentId(), "Parent comment ID mismatch");

    }
}
