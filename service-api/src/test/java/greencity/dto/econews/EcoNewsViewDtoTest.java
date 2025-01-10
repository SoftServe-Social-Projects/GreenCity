package greencity.dto.econews;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class EcoNewsViewDtoTest {

    @Test
    void isEmptyTrue() {
        EcoNewsViewDto ecoNewsViewDto = new EcoNewsViewDto("", "", "", "", "", "", "", "");
        assertTrue(ecoNewsViewDto.isEmpty());
    }

    @ParameterizedTest
    @CsvSource(value = {"1, title, author, text, 2020.12.12, 2021.02.01, News, true",
        "'', title, author, text, 2020.12.12, 2021.02.01, News, true",
        "1, '', author, text, 2020.12.12, 2021.02.01, News, true",
        "1, title, '', text, 2020.12.12, 2021.02.01, News, true",
        "1, title, author, '', 2020.12.12, 2021.02.01, News, true",
        "1, title, author, text, '', 2021.02.01, News, true",
        "1, title, author, text, 2020.12.12, '', News, true",
        "1, title, author, text, 2020.12.12, 2021.02.01, '', true",
        "1, title, author, text, 2020.12.12, 2021.02.01, News, ''",
        "'', '', author, text, 2020.12.12, 2021.02.01, News, true",
        "'', '', '', text, 2020.12.12, 2021.02.01, News, true",
        "'', '', '', '', 2020.12.12, 2021.02.01, News, true",
        "'', '', '', '', '', 2021.02.01, News, true",
        "'', '', '', '', '', '', News, true",
        "'', '', '', '', '', '', '', true"
    })
    void isEmptyFalse(String id, String title, String author, String text, String startDate, String endDate,
        String tags, String hidden) {
        EcoNewsViewDto ecoNewsViewDto =
            new EcoNewsViewDto(id, title, author, text, startDate, endDate, tags, hidden);
        assertFalse(ecoNewsViewDto.isEmpty());
    }

    @Test
    void isEmptyTrueNullValues() {
        EcoNewsViewDto ecoNewsViewDto = new EcoNewsViewDto(null, null, null, null, null, null, null, null);
        assertTrue(ecoNewsViewDto.isEmpty());
    }
}