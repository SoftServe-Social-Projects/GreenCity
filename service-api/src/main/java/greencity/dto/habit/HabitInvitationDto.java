package greencity.dto.habit;

import greencity.dto.friends.UserFriendDto;
import lombok.Builder;

@Builder
public record HabitInvitationDto(
    Long invitationId,
    UserFriendDto inviter,
    String status,
    HabitDto habit) {
}
