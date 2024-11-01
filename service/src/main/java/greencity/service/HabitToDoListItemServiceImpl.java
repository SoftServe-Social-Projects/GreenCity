package greencity.service;

import greencity.entity.Habit;
import greencity.repository.HabitRepo;
import greencity.repository.ToDoListItemRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@AllArgsConstructor
public class HabitToDoListItemServiceImpl implements HabitToDoListItemService {
    private final ToDoListItemRepo toDoListItemRepo;
    private final HabitRepo habitRepo;

    /**
     * {@inheritDoc}
     */
    @Override
    public void unlinkToDoListItems(List<Long> toDoIds, Long habitId) {
        Habit h = habitRepo.findById(habitId).orElseThrow();
        toDoIds.forEach(sh -> h.getToDoListItems().remove(toDoListItemRepo.findById(sh).orElseThrow()));
        habitRepo.save(h);
    }
}
