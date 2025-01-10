package greencity.mapping;

import greencity.dto.factoftheday.FactOfTheDayDTO;
import greencity.dto.factoftheday.FactOfTheDayTranslationEmbeddedDTO;
import greencity.entity.FactOfTheDay;
import greencity.entity.FactOfTheDayTranslation;
import greencity.entity.Language;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class FactOfTheDayDtoMapperTest {
    @InjectMocks
    private FactOfTheDayDtoMapper factOfTheDayDtoMapper;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConvert_ShouldMapFactOfTheDayToFactOfTheDayDTO() {
        FactOfTheDayTranslation translation1 = FactOfTheDayTranslation.builder()
            .id(1L)
            .content("Fact content 1")
            .language(Language.builder()
                .id(1L)
                .code("en")
                .build())
            .build();

        FactOfTheDayTranslation translation2 = FactOfTheDayTranslation.builder()
            .id(2L)
            .content("Fact content 2")
            .language(Language.builder()
                .id(2L)
                .code("fr")
                .build())
            .build();

        FactOfTheDay fact = FactOfTheDay.builder()
            .id(1L)
            .name("Sample Fact")
            .factOfTheDayTranslations(List.of(translation1, translation2))
            .createDate(ZonedDateTime.now())
            .build();

        FactOfTheDayDTO factOfTheDayDTO = factOfTheDayDtoMapper.convert(fact);

        assertNotNull(factOfTheDayDTO);
        assertEquals(fact.getId(), factOfTheDayDTO.getId());
        assertEquals(fact.getName(), factOfTheDayDTO.getName());
        assertEquals(fact.getCreateDate(), factOfTheDayDTO.getCreateDate());
        assertEquals(2, factOfTheDayDTO.getFactOfTheDayTranslations().size());

        FactOfTheDayTranslationEmbeddedDTO dtoTranslation1 = factOfTheDayDTO.getFactOfTheDayTranslations().get(0);
        assertEquals(translation1.getId(), dtoTranslation1.getId());
        assertEquals(translation1.getContent(), dtoTranslation1.getContent());
        assertEquals(translation1.getLanguage().getId(), dtoTranslation1.getLanguage().getId());
        assertEquals(translation1.getLanguage().getCode(), dtoTranslation1.getLanguage().getCode());

        FactOfTheDayTranslationEmbeddedDTO dtoTranslation2 = factOfTheDayDTO.getFactOfTheDayTranslations().get(1);
        assertEquals(translation2.getId(), dtoTranslation2.getId());
        assertEquals(translation2.getContent(), dtoTranslation2.getContent());
        assertEquals(translation2.getLanguage().getId(), dtoTranslation2.getLanguage().getId());
        assertEquals(translation2.getLanguage().getCode(), dtoTranslation2.getLanguage().getCode());
    }

    @Test
    void testConvert_EmptyTranslations_ShouldMapCorrectly() {
        FactOfTheDay fact = FactOfTheDay.builder()
            .id(1L)
            .name("Sample Fact")
            .factOfTheDayTranslations(List.of())
            .createDate(ZonedDateTime.now())
            .build();

        FactOfTheDayDTO factOfTheDayDTO = factOfTheDayDtoMapper.convert(fact);

        assertNotNull(factOfTheDayDTO);
        assertEquals(fact.getId(), factOfTheDayDTO.getId());
        assertEquals(fact.getName(), factOfTheDayDTO.getName());
        assertEquals(fact.getCreateDate(), factOfTheDayDTO.getCreateDate());
        assertTrue(factOfTheDayDTO.getFactOfTheDayTranslations().isEmpty());
    }
}