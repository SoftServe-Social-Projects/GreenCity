package greencity.dto.comment;

import greencity.dto.user.UserVO;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class CommentVO {
    private Long id;

    private String articleType;

    private Long articleId;

    @Size(min = 1, max = 8000)
    private String text;

    @CreatedDate
    private LocalDateTime createdDate;

    @NotNull
    private LocalDateTime modifiedDate;

    private CommentVO parentComment;

    @Builder.Default
    private List<CommentVO> comments = new ArrayList<>();

    private UserVO user;

    private boolean currentUserLiked;

    @Builder.Default
    private Set<UserVO> usersLiked = new HashSet<>();

    private String status;
}
