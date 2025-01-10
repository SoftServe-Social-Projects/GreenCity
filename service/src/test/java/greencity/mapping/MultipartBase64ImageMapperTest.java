package greencity.mapping;

import greencity.exception.exceptions.NotSavedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class MultipartBase64ImageMapperTest {
    @InjectMocks
    private MultipartBase64ImageMapper multipartBase64ImageMapper;

    @Test
    void convertTestThrowsException() {
        String invalidBase64Image = "iVBORw0KGgoAAAANSUhEUgAA...";

        NotSavedException exception =
            assertThrows(NotSavedException.class, () -> multipartBase64ImageMapper.convert(invalidBase64Image));

        String expectedMessage = "Cannot convert to BASE64 image";
        String actualMessage = exception.getMessage();

        assert (actualMessage.contains(expectedMessage));
    }
}
