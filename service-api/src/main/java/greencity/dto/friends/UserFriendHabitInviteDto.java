package greencity.dto.friends;

import greencity.dto.location.UserLocationDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserFriendHabitInviteDto {
    private Boolean hasInvitation;
    private Long id;
    private String name;
    private String email;
    private Double rating;
    private Long mutualFriends;
    private String profilePicturePath;
    private Long chatId;
    private String friendStatus;
    private Long requesterId;
    private UserLocationDto userLocationDto;
}
