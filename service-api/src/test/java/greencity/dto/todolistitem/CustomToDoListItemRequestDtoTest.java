package greencity.dto.todolistitem;

import greencity.ModelUtils;
import greencity.enums.ToDoListItemStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomToDoListItemRequestDtoTest {

    void testValid(CustomToDoListItemRequestDto dto) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomToDoListItemRequestDto>> constraintViolations =
            validator.validate(dto);

        assertTrue(constraintViolations.isEmpty());
    }

    void testInvalid(CustomToDoListItemRequestDto dto) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomToDoListItemRequestDto>> constraintViolations =
            validator.validate(dto);

        assertFalse(constraintViolations.isEmpty());
    }

    @ParameterizedTest
    @ValueSource(strings = {"t", "text"})
    void validTextTest(String text) {
        var dto = ModelUtils.getCustomToDoListItemRequestDto();
        dto.setText(text);

        testValid(dto);
    }

    @ParameterizedTest
    @NullAndEmptySource
    void invalidTextTest(String text) {
        var dto = ModelUtils.getCustomToDoListItemRequestDto();
        dto.setText(text);

        testInvalid(dto);
    }

    @ParameterizedTest
    @EnumSource(ToDoListItemStatus.class)
    void validStatusTest(ToDoListItemStatus status) {
        var dto = ModelUtils.getCustomToDoListItemRequestDto();
        dto.setStatus(status.toString());

        testValid(dto);
    }

    @Test
    void invalidStatusTest() {
        var dto = ModelUtils.getCustomToDoListItemRequestDto();
        dto.setStatus(null);

        testInvalid(dto);
    }
}
