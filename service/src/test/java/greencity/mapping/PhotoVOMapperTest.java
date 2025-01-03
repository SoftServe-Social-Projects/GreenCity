package greencity.mapping;

import static greencity.ModelUtils.getPhoto;
import static greencity.ModelUtils.getPhotoVO;
import static org.junit.jupiter.api.Assertions.assertEquals;

import greencity.dto.photo.PhotoVO;
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

    @Test
    void convertWithNullFieldsTest() {
        var source = greencity.entity.Photo.builder()
            .id(null)
            .name(null)
            .user(null)
            .place(null)
            .comment(null)
            .build();
        var expected = PhotoVO.builder()
            .id(null)
            .name(null)
            .commentId(null)
            .placeId(null)
            .userId(null)
            .build();
        assertEquals(expected, mapper.convert(source));
    }

    @Test
    void convertWithPartialFieldsTest() {
        var source = greencity.entity.Photo.builder()
            .id(1L)
            .name("partial_photo")
            .user(null)
            .place(null)
            .comment(null)
            .build();

        var expected = PhotoVO.builder()
            .id(1L)
            .name("partial_photo")
            .commentId(null)
            .placeId(null)
            .userId(null)
            .build();
        assertEquals(expected, mapper.convert(source));
    }
}
