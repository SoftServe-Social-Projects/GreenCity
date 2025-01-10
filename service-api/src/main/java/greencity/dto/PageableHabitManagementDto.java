package greencity.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PageableHabitManagementDto<T> {
    private List<T> page;
    private long totalElements;
    private int currentPage;
    private int totalPages;
    private String sortModel;
}
