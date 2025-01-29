package greencity.validator;

import greencity.constant.ErrorMessage;
import greencity.dto.Sortable;
import greencity.dto.friends.UserFriendDto;
import greencity.exception.exceptions.UnsupportedSortException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SortPageableValidatorTest {
    private final SortPageableValidator sortPageableValidator = new SortPageableValidator();

    @Test
    void validateSortParametersWithValidSortField() {
        String sortableField = "id";
        Sort sort = Sort.by(sortableField);

        Class<? extends Sortable> dtoClass = UserFriendDto.class;

        sortPageableValidator.validateSortParameters(sort, dtoClass);
    }

    @Test
    void validateSortParametersWithInvalidSortField() {
        String invalidSortField = "1";
        Sort sort = Sort.by(invalidSortField);

        Class<? extends Sortable> dtoClass = UserFriendDto.class;

        UnsupportedSortException exception = assertThrows(UnsupportedSortException.class,
            () -> sortPageableValidator.validateSortParameters(sort, dtoClass));

        assertEquals(ErrorMessage.INVALID_SORTING_VALUE, exception.getMessage());
    }

    @Test
    void validateSortParametersWithInvalidDtoClass() {
        String sortableField = "id";
        Sort sort = Sort.by(sortableField);

        Class<? extends Sortable> dtoClass = UnknownTestDto.class;

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> sortPageableValidator.validateSortParameters(sort, dtoClass));

        assertEquals(ErrorMessage.INVALID_DTO_CLASS, exception.getMessage());
    }

    private static class UnknownTestDto implements Sortable {
        @Override
        public List<String> getSortableFields() {
            return Collections.emptyList();
        }
    }
}
