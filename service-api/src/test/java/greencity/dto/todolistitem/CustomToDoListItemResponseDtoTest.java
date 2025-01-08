package greencity.dto.todolistitem;

import greencity.ModelUtils;
import greencity.enums.ToDoListItemStatus;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.NullSource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomToDoListItemResponseDtoTest {

    void testValid(CustomToDoListItemResponseDto dto) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomToDoListItemResponseDto>> constraintViolations =
            validator.validate(dto);

        assertTrue(constraintViolations.isEmpty());
    }

    void testInvalid(CustomToDoListItemResponseDto dto) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomToDoListItemResponseDto>> constraintViolations =
            validator.validate(dto);

        assertFalse(constraintViolations.isEmpty());
    }

    @ParameterizedTest
    @NullSource
    @ValueSource(longs = {1L, 100L, Long.MAX_VALUE})
    void validIdTest(Long id) {
        var dto = ModelUtils.getCustomToDoListItemResponseDto();
        dto.setId(id);

        testValid(dto);
    }

    @ParameterizedTest
    @ValueSource(longs = {0L, -1L, -100L, Long.MIN_VALUE})
    void invalidIdTest(Long id) {
        var dto = ModelUtils.getCustomToDoListItemResponseDto();
        dto.setId(id);

        testInvalid(dto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"t", "text"})
    void validTextTest(String text) {
        var dto = ModelUtils.getCustomToDoListItemResponseDto();
        dto.setText(text);

        testValid(dto);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void invalidTextTest(String text) {
        var dto = ModelUtils.getCustomToDoListItemResponseDto();
        dto.setText(text);

        testInvalid(dto);
    }

    @ParameterizedTest
    @EnumSource(ToDoListItemStatus.class)
    void validStatusTest(ToDoListItemStatus status) {
        var dto = ModelUtils.getCustomToDoListItemResponseDto();
        dto.setStatus(status);

        testValid(dto);
    }

    @ParameterizedTest
    @NullSource
    void invalidStatusTest(ToDoListItemStatus status) {
        var dto = ModelUtils.getCustomToDoListItemResponseDto();
        dto.setStatus(status);

        testInvalid(dto);
    }
}
