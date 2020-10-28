package greencity.dto.econewscomment;

import greencity.enums.CommentStatus;
import java.time.LocalDateTime;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class EcoNewsCommentDto {
    @NotNull
    @Min(1)
    private Long id;

    @NotEmpty
    private LocalDateTime modifiedDate;

    private EcoNewsCommentAuthorDto author;

    private String text;

    private int replies;

    private int likes;

    private boolean currentUserLiked;

    private CommentStatus status;
}
