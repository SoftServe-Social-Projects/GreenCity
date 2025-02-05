package greencity.utils;

import greencity.entity.event.EventDateLocation;
import greencity.entity.event.EventGrade;
import lombok.experimental.UtilityClass;
import java.time.ZonedDateTime;
import java.util.List;

@UtilityClass
public class EventUtils {
    public static boolean isRelevant(List<EventDateLocation> dates) {
        if (dates == null || dates.isEmpty()) {
            return false;
        }
        return dates.getLast().getFinishDate().isAfter(ZonedDateTime.now())
            || dates.getLast().getFinishDate().isEqual(ZonedDateTime.now());
    }

    public static double calculateEventRate(List<EventGrade> eventGrades) {
        if (eventGrades == null) {
            return 0.0;
        }
        return eventGrades.stream()
            .mapToInt(EventGrade::getGrade)
            .filter(grade -> grade > 0)
            .average()
            .orElse(0.0);
    }
}
