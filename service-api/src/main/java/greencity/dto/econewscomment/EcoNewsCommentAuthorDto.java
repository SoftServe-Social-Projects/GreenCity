package greencity.dto.econewscomment;

import javax.validation.constraints.NotEmpty;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@EqualsAndHashCode
public class EcoNewsCommentAuthorDto {
    @NotEmpty
    private Long id;

    @NotEmpty
    private String name;

    @NotEmpty
    private String userProfilePicturePath;
}
