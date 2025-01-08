package greencity.service;

import greencity.dto.photo.PhotoVO;
import greencity.entity.Photo;
import greencity.repository.PhotoRepo;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.modelmapper.ModelMapper;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class PhotoServiceImplTest {
    @Mock
    ModelMapper modelMapper;
    @Mock
    PhotoRepo photoRepo;
    @InjectMocks
    PhotoServiceImpl photoService;

    @Test
    void findByNameTest() {
        Photo photo = new Photo();
        PhotoVO photoVO = new PhotoVO();
        when(photoRepo.findByName("test")).thenReturn(Optional.of(photo));
        when(modelMapper.map(photo, PhotoVO.class)).thenReturn(photoVO);
        assertEquals(photoVO, photoService.findByName("test").get());
    }

    @Test
    void findByNameEmptyTest() {
        when(photoRepo.findByName("")).thenReturn(Optional.empty());
        assertEquals(Optional.empty(), photoService.findByName(""));
    }
}