package greencity.service;

import greencity.ModelUtils;
import greencity.entity.Habit;
import greencity.entity.ShoppingListItem;
import greencity.repository.HabitRepo;
import greencity.repository.ShoppingListItemRepo;
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
class HabitShoppingListItemServiceImplTest {
    @Mock
    HabitRepo habitRepo;

    @Mock
    ShoppingListItemRepo shoppingListItemRepo;

    @InjectMocks
    HabitShoppingListItemServiceImpl habitShoppingListItemService;

    @Test
    void unlinkShoppingListItems() {
        List<Long> shopIds = List.of(1L, 2L, 3L);
        Long habitId = 1L;
        Habit h = ModelUtils.getHabit();
        ShoppingListItem shoppingListItem = ModelUtils.getShoppingListItem();

        Set<ShoppingListItem> set = new HashSet<>();
        h.setShoppingListItems(set);

        when(habitRepo.findById(habitId)).thenReturn(java.util.Optional.of(h));
        when(shoppingListItemRepo.findById(anyLong())).thenReturn(java.util.Optional.ofNullable(shoppingListItem));
        when(habitRepo.save(h)).thenReturn(h);
        habitShoppingListItemService.unlinkShoppingListItems(shopIds, habitId);

        verify(habitRepo, times(1)).save(h);
    }
}