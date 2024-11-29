package greencity.validator;

import greencity.annotations.DecodedSize;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mockito;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DecodedSizeValidatorTest {

    private final DecodedSizeValidator validator = new DecodedSizeValidator();

    @ParameterizedTest
    @CsvSource({
        "'', 0, 10, true",
        "'abc', 1, 3, true",
        "'abc', 0, 3, true",
        "'abc', 1, 2, false",
        "'abc', 2, 4, true",
        "'a&b&c', 3, 5, true",
        "'a&b&c', 0, 2, false",
        "'a&lt;&gt;&amp;', 0, 9, true",
        "'a&lt;&gt;&amp;', 0, 3, false",
        "'Hello &lt;world&gt;', 0, 20, true",
        "'Hello &lt;world&gt;', 0, 12, false",
        ", 0, 0, true",
    })
    void testDecodedSizeValidator(String input, int min, int max, boolean expectedValidity) {
        DecodedSize annotation = Mockito.mock(DecodedSize.class);
        Mockito.when(annotation.min()).thenReturn(min);
        Mockito.when(annotation.max()).thenReturn(max);

        validator.initialize(annotation);

        boolean isValid = validator.isValid(input, Mockito.mock(ConstraintValidatorContext.class));
        boolean condition = expectedValidity == isValid;
        assertTrue(condition,
            String.format("For input '%s' with min %d and max %d, expected %b but got %b", input, min, max,
                expectedValidity, isValid));
    }
}
