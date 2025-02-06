package greencity.dto.econews;

import greencity.dto.Sortable;
import greencity.dto.user.EcoNewsAuthorDto;
import greencity.enums.SortableFields;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Getter;
import lombok.Builder;
import org.springframework.format.annotation.DateTimeFormat;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import java.time.ZonedDateTime;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@ToString(exclude = "author")
@Builder
@EqualsAndHashCode
public class EcoNewsGenericDto implements Sortable {
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

    private int countOfEcoNews;

    private boolean isFavorite;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getSortableFields() {
        return List.of(
            SortableFields.ID.getFieldName(),
            SortableFields.TITLE.getFieldName(),
            SortableFields.CREATION_DATE.getFieldName(),
            SortableFields.LIKES.getFieldName(),
            SortableFields.COUNT_COMMENTS.getFieldName(),
            SortableFields.COUNT_OF_ECO_NEWS.getFieldName());
    }
}
