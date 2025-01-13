package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.factoftheday.FactOfTheDayTranslationDTO;
import greencity.dto.factoftheday.FactOfTheDayTranslationEmbeddedPostDTO;
import greencity.entity.FactOfTheDay;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;
import java.util.Collections;
import static org.assertj.core.api.Assertions.assertThat;

class FactOfTheDayTranslationDTOMapperTest {

    private FactOfTheDayTranslationDTOMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FactOfTheDayTranslationDTOMapper();
    }

    @Test
    void convert_ShouldMapFactOfTheDayToDTO() {
        FactOfTheDay factOfTheDay = ModelUtils.getFactOfTheDay();

        FactOfTheDayTranslationDTO dto = mapper.convert(factOfTheDay);

        assertThat(dto.getId()).isEqualTo(1L);
        assertThat(dto.getFactOfTheDayTranslations()).hasSize(2);

        FactOfTheDayTranslationEmbeddedPostDTO embeddedDTO2 = dto.getFactOfTheDayTranslations().get(0);
        assertThat(embeddedDTO2.getContent()).isEqualTo("Content");
        assertThat(embeddedDTO2.getLanguageCode()).isEqualTo("en");

        FactOfTheDayTranslationEmbeddedPostDTO embeddedDTO1 = dto.getFactOfTheDayTranslations().get(1);
        assertThat(embeddedDTO1.getContent()).isEqualTo("Контент");
        assertThat(embeddedDTO1.getLanguageCode()).isEqualTo("ua");
    }

    @Test
    void convert_ShouldReturnEmptyList_WhenNoTranslations() {
        FactOfTheDay factOfTheDay = new FactOfTheDay(2L, "Fact of the day", Collections.emptyList(),
            ZonedDateTime.now(), Collections.emptySet());

        FactOfTheDayTranslationDTO dto = mapper.convert(factOfTheDay);

        assertThat(dto.getId()).isEqualTo(2L);
        assertThat(dto.getFactOfTheDayTranslations()).isEmpty();
    }
}