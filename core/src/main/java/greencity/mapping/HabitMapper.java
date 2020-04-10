package greencity.mapping;

import greencity.entity.Habit;
import greencity.entity.User;
import java.time.ZonedDateTime;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class HabitMapper extends AbstractConverter<User, Habit> {
    /**
     * Convert to habit entity.
     *
     * @param user {@link User} current user.
     * @return {@link Habit}
     */
    @Override
    protected Habit convert(User user) {
        Habit habit = new Habit();
        habit.setUser(user);
        habit.setCreateDate(ZonedDateTime.now());
        habit.setStatusHabit(true);
        return habit;
    }
}
