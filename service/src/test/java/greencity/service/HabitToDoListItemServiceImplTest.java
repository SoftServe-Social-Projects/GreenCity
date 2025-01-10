package greencity.service;

import greencity.ModelUtils;
import greencity.entity.Habit;
import greencity.entity.ToDoListItem;
import greencity.repository.HabitRepo;
import greencity.repository.ToDoListItemRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HabitToDoListItemServiceImplTest {
    @Mock
    HabitRepo habitRepo;

    @Mock
    ToDoListItemRepo toDoListItemRepo;

    @InjectMocks
    HabitToDoListItemServiceImpl habitToDoListItemService;

    @Test
    void unlinkToDoListItems() {
        List<Long> toDoIds = List.of(1L, 2L, 3L);
        Long habitId = 1L;
        Habit h = ModelUtils.getHabit();
        ToDoListItem toDoListItem = ModelUtils.getToDoListItem();

        Set<ToDoListItem> set = new HashSet<>();
        h.setToDoListItems(set);

        when(habitRepo.findById(habitId)).thenReturn(java.util.Optional.of(h));
        when(toDoListItemRepo.findById(anyLong())).thenReturn(java.util.Optional.ofNullable(toDoListItem));
        when(habitRepo.save(h)).thenReturn(h);
        habitToDoListItemService.unlinkToDoListItems(toDoIds, habitId);

        verify(habitRepo, times(1)).save(h);
    }
}