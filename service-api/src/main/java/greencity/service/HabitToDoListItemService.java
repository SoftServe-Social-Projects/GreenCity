package greencity.service;

import greencity.dto.todolistitem.ToDoListItemVO;
import java.util.List;

public interface HabitToDoListItemService {
    /**
     * Method to unlink ToDoListItems from Habit.
     *
     * @param habitId {@code Habit} id.
     * @param toDoIds list of {@link ToDoListItemVO} ids.
     * @author Vira Maksymets
     */
    void unlinkToDoListItems(List<Long> toDoIds, Long habitId);
}
