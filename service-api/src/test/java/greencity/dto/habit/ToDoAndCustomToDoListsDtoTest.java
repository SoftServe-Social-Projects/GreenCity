package greencity.dto.habit;

import greencity.ModelUtils;
import org.junit.jupiter.api.Test;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.List;
import java.util.Set;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ToDoAndCustomToDoListsDtoTest {

    void testValid(ToDoAndCustomToDoListsDto dto) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<ToDoAndCustomToDoListsDto>> constraintViolations =
            validator.validate(dto);

        assertTrue(constraintViolations.isEmpty());
    }

    void testInvalid(ToDoAndCustomToDoListsDto dto) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<ToDoAndCustomToDoListsDto>> constraintViolations =
            validator.validate(dto);

        assertFalse(constraintViolations.isEmpty());
    }

    @Test
    void validTest() {
        var dto = ModelUtils.getToDoAndCustomToDoListsDto();

        testValid(dto);
    }

    @Test
    void invalidTestWithInvalidUserToDoList() {
        var toDoDto = ModelUtils.getToDoListItemWithStatusRequestDto();
        toDoDto.setId(-1L);
        var dto = ModelUtils.getToDoAndCustomToDoListsDto();
        dto.setToDoListItemDto(List.of(toDoDto));

        testInvalid(dto);
    }

    @Test
    void invalidTestWithInvalidCustomToDoList() {
        var customDto = ModelUtils.getCustomToDoListItemRequestDto();
        customDto.setText("");
        var dto = ModelUtils.getToDoAndCustomToDoListsDto();
        dto.setCustomToDoListItemDto(List.of(customDto));

        testInvalid(dto);
    }
}
