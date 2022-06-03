package greencity.dto.econews;

import greencity.dto.user.EcoNewsAuthorDto;
import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import java.time.ZonedDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString(exclude = "author")
@Builder
@EqualsAndHashCode
public class EcoNewsGenericDto {
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
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private ZonedDateTime creationDate;

    @NotEmpty
    private String imagePath;

    private String source;

    @NotEmpty
    private List<String> tagsUa;

    @NotEmpty
    private List<String> tagsEn;

    private int likes;

    private int countComments;
}
