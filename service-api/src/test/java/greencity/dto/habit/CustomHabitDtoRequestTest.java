package greencity.dto.habit;

import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CustomHabitDtoRequestTest {

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndValidValues")
    void validComplexityInAddCustomHabitDtoRequestTest(Integer complexity) {
        var dto = CustomHabitDtoRequest.builder()
            .complexity(complexity)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomHabitDtoRequest>> constraintViolations =
            validator.validate(dto);

        assertTrue(constraintViolations.isEmpty());
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndInvalidValues")
    void invalidComplexityInAddCustomHabitDtoRequestTest(Integer complexity) {
        var dto = CustomHabitDtoRequest.builder()
            .complexity(complexity)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomHabitDtoRequest>> constraintViolations =
            validator.validate(dto);

        assertEquals(1, constraintViolations.size());
    }

    @Test
    void invalidComplexityIsNullInAddCustomHabitDtoRequestTest() {
        var dto = CustomHabitDtoRequest.builder()
            .complexity(null)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomHabitDtoRequest>> constraintViolations =
            validator.validate(dto);

        assertEquals(1, constraintViolations.size());
    }

    private static Stream<Arguments> provideFieldsAndValidValues() {
        return Stream.of(
            Arguments.of(1),
            Arguments.of(2),
            Arguments.of(3));
    }

    private static Stream<Arguments> provideFieldsAndInvalidValues() {
        return Stream.of(
            Arguments.of(4),
            Arguments.of(5),
            Arguments.of(-6),
            Arguments.of(-1),
            Arguments.of(0));
    }

    @Test
    void invalidNumberOfTagsIdNullInAddCustomHabitDtoRequestTest() {
        var dto = CustomHabitDtoRequest.builder()
            .tagIds(null)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomHabitDtoRequest>> constraintViolations =
            validator.validate(dto);

        assertEquals(1, constraintViolations.size());
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndValidValuesForTagsIds")
    void validNumberOfTagsIdsInAddCustomHabitDtoRequestTest(Set<Long> tagIds) {
        var dto = CustomHabitDtoRequest.builder()
            .tagIds(tagIds)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomHabitDtoRequest>> constraintViolations =
            validator.validate(dto);

        assertEquals(1, constraintViolations.size());
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndInvalidValuesForTagsIds")
    void invalidNumberOfTagsIdsInAddCustomHabitDtoRequestTest(Set<Long> tagIds) {
        var dto = CustomHabitDtoRequest.builder()
            .tagIds(tagIds)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomHabitDtoRequest>> constraintViolations =
            validator.validate(dto);

        assertEquals(2, constraintViolations.size());
    }

    private static Stream<Arguments> provideFieldsAndValidValuesForTagsIds() {
        return Stream.of(
            Arguments.of(Set.of(1L)),
            Arguments.of(Set.of(1L, 2L, 3L)),
            Arguments.of(Set.of(1L, 2L)));
    }

    private static Stream<Arguments> provideFieldsAndInvalidValuesForTagsIds() {
        return Stream.of(
            Arguments.of(Set.of(1L, 2L, 3L, 4L)),
            Arguments.of(Set.of()),
            Arguments.of(Set.of(1L, 2L, 3L, 4L, 5L, 6L)));
    }
}