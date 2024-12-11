package greencity.dto.friends;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserFriendHabitInviteDto extends UserFriendDto {
    private Boolean hasInvitation;

    public UserFriendHabitInviteDto(UserFriendDto userFriendDto, Boolean hasInvitation) {
        super(
            userFriendDto.getId(),
            userFriendDto.getName(),
            userFriendDto.getEmail(),
            userFriendDto.getRating(),
            userFriendDto.getMutualFriends(),
            userFriendDto.getProfilePicturePath(),
            userFriendDto.getChatId(),
            userFriendDto.getFriendStatus(),
            userFriendDto.getRequesterId(),
            userFriendDto.getUserLocationDto());
        this.hasInvitation = hasInvitation;
    }
}
