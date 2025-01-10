package greencity.dto.econews;

import greencity.dto.user.EcoNewsAuthorDto;
import java.time.ZonedDateTime;
import java.util.List;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

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

    private int dislikes;

    private int countComments;

    private boolean hidden;
}
