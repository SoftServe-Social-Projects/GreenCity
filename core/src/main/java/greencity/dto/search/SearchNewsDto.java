package greencity.dto.search;

import greencity.dto.user.EcoNewsAuthorDto;
import java.time.ZonedDateTime;
import java.util.List;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;
import lombok.*;

@Setter
@Getter
@Builder
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SearchNewsDto {
    @Min(0)
    private Long id;
    @NotEmpty
    private String title;
    @NotEmpty
    private EcoNewsAuthorDto author;
    @NotEmpty
    private ZonedDateTime creationDate;
    @NotEmpty
    private List<String> tags;
}

