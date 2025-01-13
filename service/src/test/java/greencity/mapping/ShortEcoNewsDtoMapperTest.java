package greencity.mapping;

import greencity.dto.econews.EcoNewsDto;
import greencity.dto.econews.ShortEcoNewsDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static greencity.ModelUtils.getEcoNewsDto;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class ShortEcoNewsDtoMapperTest {

    @InjectMocks
    private ShortEcoNewsDtoMapper mapper;

    @Test
    void convert() {
        EcoNewsDto ecoNewsDto = getEcoNewsDto();

        ShortEcoNewsDto expected = ShortEcoNewsDto.builder()
            .ecoNewsId(ecoNewsDto.getId())
            .title(ecoNewsDto.getTitle())
            .imagePath(ecoNewsDto.getImagePath())
            .text(ecoNewsDto.getContent())
            .build();

        ShortEcoNewsDto actual = mapper.convert(ecoNewsDto);

        assertEquals(expected, actual);
    }
}