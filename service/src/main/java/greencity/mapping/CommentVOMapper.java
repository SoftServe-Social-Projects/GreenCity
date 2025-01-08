package greencity.mapping;

import greencity.dto.comment.CommentVO;
import greencity.dto.user.UserVO;
import greencity.entity.Comment;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;
import java.util.stream.Collectors;

/**
 * Class that used by {@link ModelMapper} to map {@link Comment} into
 * {@link CommentVO}.
 */

@Component
public class CommentVOMapper extends AbstractConverter<Comment, CommentVO> {
    @Override
    public CommentVO convert(Comment comment) {
        return CommentVO.builder()
            .id(comment.getId())
            .text(comment.getText())
            .articleType(comment.getArticleType().toString())
            .articleId(comment.getArticleId() != null ? comment.getArticleId() : null)
            .createdDate(comment.getCreatedDate())
            .modifiedDate(comment.getModifiedDate())
            .parentComment(comment.getParentComment() != null ? CommentVO.builder()
                .id(comment.getParentComment().getId())
                .build() : null)
            .user(UserVO.builder()
                .id(comment.getUser().getId())
                .role(comment.getUser().getRole())
                .name(comment.getUser().getName())
                .build())
            .currentUserLiked(comment.isCurrentUserLiked())
            .usersLiked(comment.getUsersLiked() != null ? comment.getUsersLiked().stream()
                .map(user -> UserVO.builder()
                    .id(user.getId())
                    .build())
                .collect(Collectors.toSet()) : null)
            .status(comment.getStatus() != null ? comment.getStatus().toString() : null)
            .build();
    }
}
