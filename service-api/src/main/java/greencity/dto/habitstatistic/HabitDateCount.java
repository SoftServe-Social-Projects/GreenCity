package greencity.dto.habitstatistic;

import java.time.LocalDate;

public record HabitDateCount(LocalDate date, Long count) {
    public HabitDateCount(java.sql.Date date, Long count) {
        this(date.toLocalDate(), count);
    }
}
