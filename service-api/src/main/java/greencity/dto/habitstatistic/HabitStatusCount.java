package greencity.dto.habitstatistic;

import greencity.enums.HabitAssignStatus;

public record HabitStatusCount(HabitAssignStatus status, long count) {
}
