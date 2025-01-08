package greencity.mapping;

import greencity.dto.comment.AddCommentDtoResponse;
import greencity.entity.Comment;
import greencity.entity.CommentImages;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link Comment} into
 * {@link AddCommentDtoResponse}.
 */
@Component
public class AddCommentDtoResponseMapper extends AbstractConverter<Comment, AddCommentDtoResponse> {
    /**
     * Method for converting {@link Comment} into {@link AddCommentDtoResponse}.
     *
     * @param comment object to convert.
     * @return converted object.
     */
    @Override
    protected AddCommentDtoResponse convert(Comment comment) {
        AddCommentDtoResponse addCommentDtoResponse = new AddCommentDtoResponse();
        addCommentDtoResponse.setId(comment.getId());
        addCommentDtoResponse.setText(comment.getText());
        addCommentDtoResponse.setCreatedDate(comment.getCreatedDate());
        if (comment.getAdditionalImages() != null) {
            addCommentDtoResponse
                .setAdditionalImages(comment.getAdditionalImages().stream().map(CommentImages::getLink).toList());
        }
        return addCommentDtoResponse;
    }
}
