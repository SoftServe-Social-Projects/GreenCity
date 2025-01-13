package greencity.mapping;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import greencity.ModelUtils;
import greencity.dto.tag.NewTagDto;
import greencity.entity.Tag;

import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(SpringExtension.class)
class NewTagDtoMapperTest {
    @InjectMocks
    NewTagDtoMapper newTagDtoMapper;

    @Test
    void convertTest() {
        Tag tag = ModelUtils.getTag();

        NewTagDto expected = NewTagDto.builder()
            .id(1L)
            .nameUa("Новини")
            .name("News")
            .build();

        assertEquals(expected, newTagDtoMapper.convert(tag));
    }
}
