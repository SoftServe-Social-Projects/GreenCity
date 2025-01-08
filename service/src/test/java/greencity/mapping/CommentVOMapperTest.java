package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.comment.CommentVO;
import greencity.dto.user.UserVO;
import greencity.entity.Comment;
import greencity.service.CommentService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@ExtendWith(MockitoExtension.class)
class CommentVOMapperTest {
    @InjectMocks
    CommentVOMapper commentVOMapper;
    @Mock
    CommentService commentService;

    @Test
    void convertTest() {
        Comment parentComment = ModelUtils.getComment().setId(2L);
        Comment comment = ModelUtils.getComment().setParentComment(parentComment);
        comment.setArticleId(1L);
        CommentVO actual = commentVOMapper.convert(comment);
        Set<UserVO> usersLiked = comment.getUsersLiked().stream()
            .map(user -> UserVO.builder()
                .id(user.getId())
                .build())
            .collect(Collectors.toSet());

        assertEquals(comment.getId(), actual.getId());
        assertEquals(comment.getText(), actual.getText());
        assertEquals(comment.getArticleId(), actual.getArticleId());
        assertEquals(comment.getCreatedDate(), actual.getCreatedDate());
        assertEquals(comment.getArticleType().toString(), actual.getArticleType());
        assertEquals(comment.getParentComment().getId(), actual.getParentComment().getId());
        assertEquals(comment.getUser().getId(), actual.getUser().getId());
        assertEquals(comment.getUser().getName(), actual.getUser().getName());
        assertEquals(comment.getUser().getRole().name(), actual.getUser().getRole().name());
        assertEquals(comment.getStatus().toString(), actual.getStatus());
        assertEquals(comment.getText(), actual.getText());
        assertEquals(comment.isCurrentUserLiked(), actual.isCurrentUserLiked());
        assertEquals(usersLiked, actual.getUsersLiked());
    }

    @Test
    void convertTestWithNullValues() {
        Comment comment = ModelUtils.getComment()
            .setParentComment(null)
            .setStatus(null)
            .setArticleId(null);

        CommentVO actual = commentVOMapper.convert(comment);

        Set<UserVO> usersLiked = comment.getUsersLiked().stream()
            .map(user -> UserVO.builder()
                .id(user.getId())
                .build())
            .collect(Collectors.toSet());

        assertEquals(comment.getId(), actual.getId());
        assertEquals(comment.getText(), actual.getText());
        assertEquals(comment.getCreatedDate(), actual.getCreatedDate());
        assertEquals(comment.getArticleType().toString(), actual.getArticleType());
        assertNull(actual.getParentComment());
        assertEquals(comment.getUser().getId(), actual.getUser().getId());
        assertEquals(comment.getUser().getName(), actual.getUser().getName());
        assertEquals(comment.getUser().getRole().name(), actual.getUser().getRole().name());
        assertNull(actual.getStatus());
        assertEquals(comment.isCurrentUserLiked(), actual.isCurrentUserLiked());
        assertEquals(usersLiked, actual.getUsersLiked());
    }

}
