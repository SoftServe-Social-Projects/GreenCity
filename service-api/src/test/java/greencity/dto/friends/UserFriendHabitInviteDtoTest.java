package greencity.dto.friends;

import greencity.dto.location.UserLocationDto;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class UserFriendHabitInviteDtoTest {

    @Test
    public void testConstructorWithUserFriendDto() {
        UserFriendDto userFriendDto = new UserFriendDto(
                1L, "John Doe", "john.doe@example.com", 4.5, 10L,
                "path/to/picture", 123L, "Friend", 456L, new UserLocationDto()
        );

        UserFriendHabitInviteDto inviteDto = new UserFriendHabitInviteDto(userFriendDto, true);

        assertNotNull(inviteDto);
        assertEquals(1L, inviteDto.getId());
        assertEquals("John Doe", inviteDto.getName());
        assertEquals("john.doe@example.com", inviteDto.getEmail());
        assertEquals(4.5, inviteDto.getRating());
        assertEquals(10L, inviteDto.getMutualFriends());
        assertEquals("path/to/picture", inviteDto.getProfilePicturePath());
        assertEquals(123L, inviteDto.getChatId());
        assertEquals("Friend", inviteDto.getFriendStatus());
        assertEquals(456L, inviteDto.getRequesterId());
        assertNotNull(inviteDto.getUserLocationDto());
        assertTrue(inviteDto.getHasInvitation());
    }
}
