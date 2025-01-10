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

class CustomHabitDtoResponseTest {

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndValidValues")
    void validComplexityInAddCustomHabitDtoResponseTest(Integer complexity) {
        var dto = CustomHabitDtoResponse.builder()
            .complexity(complexity)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomHabitDtoResponse>> constraintViolations =
            validator.validate(dto);

        assertTrue(constraintViolations.isEmpty());
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndInvalidValues")
    void invalidComplexityInAddCustomHabitDtoResponseTest(Integer complexity) {
        var dto = CustomHabitDtoResponse.builder()
            .complexity(complexity)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomHabitDtoResponse>> constraintViolations =
            validator.validate(dto);

        assertEquals(1, constraintViolations.size());
    }

    @Test
    void invalidComplexityIsNullInAddCustomHabitDtoResponseTest() {
        var dto = CustomHabitDtoResponse.builder()
            .complexity(null)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomHabitDtoResponse>> constraintViolations =
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

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndValidValuesForTagsIds")
    void validNumberOfTagsIdsInAddCustomHabitDtoResponseTest(Set<Long> tagIds) {
        var dto = CustomHabitDtoResponse.builder()
            .tagIds(tagIds)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomHabitDtoResponse>> constraintViolations =
            validator.validate(dto);

        assertEquals(1, constraintViolations.size());
    }

    @SneakyThrows
    @ParameterizedTest
    @MethodSource("provideFieldsAndInvalidValuesForTagsIds")
    void invalidNumberOfTagsIdsInAddCustomHabitDtoResponseTest(Set<Long> tagIds) {
        var dto = CustomHabitDtoResponse.builder()
            .tagIds(tagIds)
            .build();

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        final Validator validator = factory.getValidator();

        Set<ConstraintViolation<CustomHabitDtoResponse>> constraintViolations =
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