package greencity.dto.econews;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
@ToString
public class EcoNewsViewDto {
    private String id;
    private String title;
    private String author;
    private String text;
    private String startDate;
    private String endDate;
    private String tags;
    private String hidden;

    /**
     * This method check if object is empty.
     */
    public boolean isEmpty() {
        return (id == null || id.isEmpty())
            && (title == null || title.isEmpty())
            && (author == null || author.isEmpty())
            && (text == null || text.isEmpty())
            && (startDate == null || startDate.isEmpty())
            && (endDate == null || endDate.isEmpty())
            && (tags == null || tags.isEmpty())
            && (hidden == null || hidden.isEmpty());
    }
}
