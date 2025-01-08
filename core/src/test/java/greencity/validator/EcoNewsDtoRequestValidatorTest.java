package greencity.validator;

import static greencity.ModelUtils.getAddEcoNewsDtoRequest;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.exception.exceptions.InvalidURLException;
import java.util.Collections;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class EcoNewsDtoRequestValidatorTest {
    @InjectMocks
    private EcoNewsDtoRequestValidator validator;

    @Test
    void isValidTrueTest() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();
        request.setSource("https://eco-lavca.ua/");
        assertTrue(validator.isValid(request, null));
    }

    @Test
    void isValidWrongCountOfTagsExceptionTest() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();
        request.setTags(Collections.emptyList());
        request.setSource("https://eco-lavca.ua/");
        assertFalse(validator.isValid(request, null));
    }

    @Test
    void isValidInvalidURLExceptionTest() {
        AddEcoNewsDtoRequest request = getAddEcoNewsDtoRequest();
        request.setSource("/eco-lavca.ua/'s%83");
        assertThrows(InvalidURLException.class, () -> validator.isValid(request, null));
    }
}
