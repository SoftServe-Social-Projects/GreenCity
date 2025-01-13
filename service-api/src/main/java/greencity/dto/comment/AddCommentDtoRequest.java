package greencity.dto.comment;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class AddCommentDtoRequest {
    @NotBlank(message = "Text must not be null and must contain at least one non-whitespace character.")
    @Length(min = 1, max = 8000)
    private String text;

    private Long parentCommentId;
}
