package greencity.utils;

import greencity.entity.event.EventDateLocation;
import greencity.entity.event.EventGrade;
import org.junit.jupiter.api.Test;
import java.time.ZonedDateTime;
import java.util.List;
import static greencity.ModelUtils.createEventDateLocation;
import static org.junit.jupiter.api.Assertions.*;

class EventUtilsTest {
    @Test
    void isRelevantFalseWhenDatesListIsNullTest() {
        assertFalse(EventUtils.isRelevant(null));
    }

    @Test
    void isRelevantFalseWhenDatesListIsEmptyTest() {
        assertFalse(EventUtils.isRelevant(List.of()));
    }

    @Test
    void isRelevantFalseWhenAllEventsAreInPastTest() {
        List<EventDateLocation> pastEvents = List.of(
            createEventDateLocation(ZonedDateTime.now().minusDays(10), ZonedDateTime.now().minusDays(5)),
            createEventDateLocation(ZonedDateTime.now().minusDays(4), ZonedDateTime.now().minusDays(1)));

        assertFalse(EventUtils.isRelevant(pastEvents));
    }

    @Test
    void isRelevantTrueWhenLatestEventIsInFutureTest() {
        List<EventDateLocation> mixedEvents = List.of(
            createEventDateLocation(ZonedDateTime.now().minusDays(10), ZonedDateTime.now().minusDays(5)),
            createEventDateLocation(ZonedDateTime.now().plusDays(2), ZonedDateTime.now().plusDays(5)));

        assertTrue(EventUtils.isRelevant(mixedEvents));
    }

    @Test
    void calculateEventRateReturnsCorrectAverageTest() {
        List<EventGrade> grades = List.of(
            EventGrade.builder().grade(4).build(),
            EventGrade.builder().grade(5).build(),
            EventGrade.builder().grade(3).build());
        assertEquals(4.0, EventUtils.calculateEventRate(grades));
    }

    @Test
    void calculateEventRateReturnsSingleValueForOneGradeTest() {
        List<EventGrade> grades = List.of(EventGrade.builder().grade(5).build());
        assertEquals(5.0, EventUtils.calculateEventRate(grades));
    }

    @Test
    void calculateEventRateReturnsZeroForEmptyListTest() {
        assertEquals(0.0, EventUtils.calculateEventRate(List.of()));
    }

    @Test
    void calculateEventRateHandlesNullInputTest() {
        assertEquals(0.0, EventUtils.calculateEventRate(null));
    }

    @Test
    void calculateEventRateHandlesInvalidGradesTest() {
        List<EventGrade> grades = List.of(
            EventGrade.builder().grade(-1).build(),
            EventGrade.builder().grade(6).build());
        assertEquals(6.0, EventUtils.calculateEventRate(grades));
    }
}
