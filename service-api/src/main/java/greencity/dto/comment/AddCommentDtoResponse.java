package greencity.dto.comment;

import jakarta.annotation.Nullable;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AddCommentDtoResponse {
    @NotNull
    @Min(1)
    private Long id;

    @NotNull
    private CommentAuthorDto author;

    @NotBlank
    private String text;

    @CreatedDate
    private LocalDateTime createdDate;

    @Nullable
    @Max(5)
    private List<String> additionalImages;
}
