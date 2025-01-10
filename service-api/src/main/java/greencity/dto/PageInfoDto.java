package greencity.dto;

import java.util.List;

public record PageInfoDto(int currentPage, int totalPages, List<Integer> pageNumbers) {
}
