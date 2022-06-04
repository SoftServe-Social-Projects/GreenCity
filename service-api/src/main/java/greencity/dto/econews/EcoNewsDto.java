package greencity.dto.econews;

import greencity.dto.user.EcoNewsAuthorDto;
import java.time.ZonedDateTime;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString(exclude = "author")
@Builder
@EqualsAndHashCode
public class EcoNewsDto {
    @NotEmpty
    private ZonedDateTime creationDate;

    @NotEmpty
    private String imagePath;

    @NotNull
    @Min(1)
    private Long id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;

    private String shortInfo;
    @NotEmpty
    private EcoNewsAuthorDto author;

    @NotEmpty
    private List<String> tags;

    @NotEmpty
    private List<String> tagsUa;

    private int likes;

    private int countComments;
}
