package greencity.mapping;

import greencity.dto.habit.HabitAssignDto;
import greencity.dto.user.UserToDoListItemAdvanceDto;
import greencity.entity.*;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.List;

@Component
public class HabitAssignMapper extends AbstractConverter<HabitAssignDto, HabitAssign> {
    @Override
    protected HabitAssign convert(HabitAssignDto dto) {
        List<UserToDoListItem> listOfShoppingListItem = new ArrayList<>();
        for (UserToDoListItemAdvanceDto item : dto.getUserToDoListItems()) {
            listOfShoppingListItem.add(UserToDoListItem.builder()
                .id(item.getId())
                .dateCompleted(item.getDateCompleted())
                .status(item.getStatus())
                .targetId(item.getTargetId())
                .isCustomItem(item.getIsCustomItem())
                .build());
        }
        return HabitAssign.builder()
            .id(dto.getId())
            .duration(dto.getDuration())
            .habitStreak(dto.getHabitStreak())
            .createDate(dto.getCreateDateTime())
            .status(dto.getStatus())
            .workingDays(dto.getWorkingDays())
            .lastEnrollmentDate(dto.getLastEnrollmentDate())
            .habit(Habit.builder()
                .id(dto.getHabit().getId())
                .complexity(dto.getHabit().getComplexity())
                .defaultDuration(dto.getDuration())
                .build())
            .userToDoListItems(listOfShoppingListItem)
            .build();
    }
}
