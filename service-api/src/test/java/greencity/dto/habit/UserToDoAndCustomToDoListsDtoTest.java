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

class UserToDoAndCustomToDoListsDtoTest {

    void testValid(UserToDoAndCustomToDoListsDto dto) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<UserToDoAndCustomToDoListsDto>> constraintViolations =
            validator.validate(dto);

        assertTrue(constraintViolations.isEmpty());
    }

    void testInvalid(UserToDoAndCustomToDoListsDto dto) {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<UserToDoAndCustomToDoListsDto>> constraintViolations =
            validator.validate(dto);

        assertFalse(constraintViolations.isEmpty());
    }

    @Test
    void validTest() {
        var dto = ModelUtils.getUserToDoAndCustomToDoListsDto();

        testValid(dto);
    }

    @Test
    void invalidTestWithInvalidUserToDoList() {
        var userDto = ModelUtils.getUserToDoListItemResponseDto();
        userDto.setId(-1L);
        var dto = ModelUtils.getUserToDoAndCustomToDoListsDto();
        dto.setUserToDoListItemDto(List.of(userDto));

        testInvalid(dto);
    }

    @Test
    void invalidTestWithInvalidCustomToDoList() {
        var customDto = ModelUtils.getCustomToDoListItemResponseDto();
        customDto.setId(-1L);
        var dto = ModelUtils.getUserToDoAndCustomToDoListsDto();
        dto.setCustomToDoListItemDto(List.of(customDto));

        testInvalid(dto);
    }
}
