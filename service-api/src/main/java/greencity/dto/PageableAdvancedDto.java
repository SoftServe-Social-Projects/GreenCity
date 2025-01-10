package greencity.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
public class PageableAdvancedDto<T> {
    /**
     * Constructor.
     */
    @JsonCreator
    public PageableAdvancedDto(@JsonProperty("page") List<T> page,
        @JsonProperty("totalElements") long totalElements,
        @JsonProperty("currentPage") int currentPage,
        @JsonProperty("totalPages") int totalPages,
        @JsonProperty("number") int number,
        @JsonProperty("hasPrevious") boolean hasPrevious,
        @JsonProperty("hasNext") boolean hasNext,
        @JsonProperty("first") boolean first,
        @JsonProperty("last") boolean last) {
        this.page = page;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.totalPages = totalPages;
        this.number = number;
        this.hasPrevious = hasPrevious;
        this.hasNext = hasNext;
        this.first = first;
        this.last = last;
    }

    private List<T> page;

    private long totalElements;

    private int currentPage;

    private int totalPages;

    private int number;

    private boolean hasPrevious;

    private boolean hasNext;

    private boolean first;

    private boolean last;
}
