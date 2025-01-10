package greencity.dto.comment;

import jakarta.validation.constraints.NotEmpty;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class AmountCommentLikesDto {
    @NotEmpty
    private Long id;

    private Integer amountLikes;

    private Long userId;

    private boolean isLiked;
}
