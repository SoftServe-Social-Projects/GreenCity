package greencity.mapping;

import greencity.ModelUtils;
import greencity.dto.shoppinglistitem.ShoppingListItemVO;
import greencity.dto.habit.HabitAssignVO;
import greencity.dto.user.UserShoppingListItemVO;
import greencity.entity.UserShoppingListItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserShoppingListItemMapperTest {
    @InjectMocks
    private UserShoppingListItemMapper mapper;

    @Test
    void convert() {
        UserShoppingListItem expected = ModelUtils.getUserShoppingListItem();
        UserShoppingListItemVO toConvert = UserShoppingListItemVO.builder()
            .id(expected.getId())
            .status(expected.getStatus())
            .habitAssign(HabitAssignVO.builder()
                .id(expected.getHabitAssign().getId())
                .status(expected.getHabitAssign().getStatus())
                .habitStreak(expected.getHabitAssign().getHabitStreak())
                .duration(expected.getHabitAssign().getDuration())
                .lastEnrollmentDate(expected.getHabitAssign().getLastEnrollmentDate())
                .workingDays(expected.getHabitAssign().getWorkingDays())
                .build())
            .shoppingListItemVO(ShoppingListItemVO.builder()
                .id(expected.getShoppingListItem().getId())
                .build())
            .dateCompleted(expected.getDateCompleted())
            .build();

        assertEquals(expected, mapper.convert(toConvert));
    }
}
