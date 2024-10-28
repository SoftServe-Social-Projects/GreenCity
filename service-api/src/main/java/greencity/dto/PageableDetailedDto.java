package greencity.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import java.util.List;

@Data
@AllArgsConstructor
public class PageableDetailedDto<T> {
    private List<T> page;
    private long totalElements;
    private int pageNumber;
    private List<Integer> pageNumbers;
    private int totalPages;
    private String sortModel;
    private boolean isFirst;
    private boolean isLast;
}
