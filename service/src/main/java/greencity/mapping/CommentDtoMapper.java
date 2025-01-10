package greencity.mapping;

import greencity.dto.comment.CommentDto;
import greencity.entity.Comment;
import greencity.entity.CommentImages;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link Comment} into
 * {@link CommentDto}.
 */
@Component
public class CommentDtoMapper extends AbstractConverter<Comment, CommentDto> {
    /**
     * Method for converting {@link Comment} into {@link CommentDto}.
     *
     * @param comment object to convert.
     * @return converted object.
     */
    @Override
    protected CommentDto convert(Comment comment) {
        CommentDto commentDto = new CommentDto();
        commentDto.setId(comment.getId());
        commentDto.setText(comment.getText());
        commentDto.setCreatedDate(comment.getCreatedDate());
        commentDto.setModifiedDate(comment.getModifiedDate());
        if (comment.getParentComment() != null) {
            commentDto.setParentCommentId(comment.getParentComment().getId());
        }
        commentDto.setText(comment.getText());
        commentDto.setStatus(commentDto.getStatus());
        if (comment.getAdditionalImages() != null) {
            commentDto.setAdditionalImages(comment.getAdditionalImages().stream().map(CommentImages::getLink).toList());
        }
        return commentDto;
    }
}
