package greencity.dto.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class CommentAuthorDto {
    @NotNull
    private Long id;

    @NotBlank
    private String name;

    @NotBlank
    private String profilePicturePath;
}
