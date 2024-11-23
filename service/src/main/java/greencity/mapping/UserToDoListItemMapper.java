package greencity.mapping;

import greencity.dto.user.UserToDoListItemVO;
import greencity.entity.HabitAssign;
import greencity.entity.UserToDoListItem;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;

@Component
public class UserToDoListItemMapper extends AbstractConverter<UserToDoListItemVO, UserToDoListItem> {
    @Override
    protected UserToDoListItem convert(UserToDoListItemVO userToDoListItemVO) {
        return UserToDoListItem.builder()
            .id(userToDoListItemVO.getId())
            .status(userToDoListItemVO.getStatus())
            .habitAssign(HabitAssign.builder()
                .id(userToDoListItemVO.getHabitAssign().getId())
                .status(userToDoListItemVO.getHabitAssign().getStatus())
                .habitStreak(userToDoListItemVO.getHabitAssign().getHabitStreak())
                .duration(userToDoListItemVO.getHabitAssign().getDuration())
                .lastEnrollmentDate(userToDoListItemVO.getHabitAssign().getLastEnrollmentDate())
                .workingDays(userToDoListItemVO.getHabitAssign().getWorkingDays())
                .build())
            .targetId(userToDoListItemVO.getTargetId())
            .isCustomItem(userToDoListItemVO.getIsCustomItem())
            .dateCompleted(userToDoListItemVO.getDateCompleted())
            .build();
    }
}
