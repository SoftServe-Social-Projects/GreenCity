package greencity.mapping;

import greencity.dto.econewscomment.EcoNewsCommentDto;
import greencity.dto.econewscomment.EcoNewsCommentAuthorDto;
import greencity.entity.EcoNewsComment;
import greencity.enums.CommentStatus;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class EcoNewsCommentDtoMapper extends AbstractConverter<EcoNewsComment, EcoNewsCommentDto> {
    @Override
    protected EcoNewsCommentDto convert(EcoNewsComment ecoNewsComment) {
        EcoNewsCommentDto dto = new EcoNewsCommentDto();
        dto.setId(ecoNewsComment.getId());
        dto.setModifiedDate(ecoNewsComment.getModifiedDate());
        if (ecoNewsComment.isDeleted()) {
            dto.setStatus(CommentStatus.DELETED);
            return dto;
        } else if (ecoNewsComment.getCreatedDate().isEqual(ecoNewsComment.getModifiedDate())) {
            dto.setStatus(CommentStatus.ORIGINAL);
        } else {
            dto.setStatus(CommentStatus.EDITED);
        }
        dto.setText(ecoNewsComment.getText());
        dto.setAuthor(EcoNewsCommentAuthorDto.builder()
            .id(ecoNewsComment.getUser().getId())
            .name(ecoNewsComment.getUser().getName())
            .userProfilePicturePath(ecoNewsComment.getUser().getProfilePicturePath())
            .build());
        dto.setLikes(ecoNewsComment.getUsersLiked().size());
        dto.setCurrentUserLiked(ecoNewsComment.isCurrentUserLiked());
        return dto;
    }
}