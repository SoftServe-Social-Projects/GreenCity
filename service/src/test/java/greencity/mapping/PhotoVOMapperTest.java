package greencity.mapping;

import static greencity.ModelUtils.getPhoto;
import static greencity.ModelUtils.getPhotoVO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PhotoVOMapperTest {
    @InjectMocks
    private PhotoVOMapper mapper;

    @Test
    void convertTest() {
        var source = getPhoto();
        var expected = getPhotoVO();
        assertEquals(expected, mapper.convert(source));
    }
}