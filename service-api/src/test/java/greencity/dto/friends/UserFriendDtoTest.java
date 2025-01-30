package greencity.dto.friends;

import greencity.dto.location.UserLocationDto;
import greencity.enums.SortableFields;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class UserFriendDtoTest {
    @Test
    void userFriendDtoConstructorTest() {
        Long id = 1L;
        String name = "name";
        String email = "email";
        UserLocationDto uLocation = new UserLocationDto(1L, "Lviv", "Львів", "Lvivska",
            "Львівська", "Ukraine", "Україна", 12.345678, 12.345678);
        Double rating = 1.0;
        Long mutualFriends = 2L;
        String profilePicture = "profilePicture";
        Long chatId = 4L;
        String friendStatus = "FRIEND";
        Long requesterId = 3L;
        UserFriendDto userFriendDto = new UserFriendDto(id, name, email, rating, uLocation.getId(),
            uLocation.getCityEn(), uLocation.getCityUa(), uLocation.getRegionEn(), uLocation.getRegionUa(),
            uLocation.getCountryEn(), uLocation.getCountryUa(), uLocation.getLatitude(), uLocation.getLongitude(),
            mutualFriends, profilePicture, chatId, friendStatus, requesterId);

        assertEquals(id, userFriendDto.getId());
        assertEquals(name, userFriendDto.getName());
        assertEquals(uLocation, userFriendDto.getUserLocationDto());
        assertEquals(rating, userFriendDto.getRating());
        assertEquals(mutualFriends, userFriendDto.getMutualFriends());
        assertEquals(profilePicture, userFriendDto.getProfilePicturePath());
        assertEquals(chatId, userFriendDto.getChatId());
        assertEquals(friendStatus, userFriendDto.getFriendStatus());
        assertEquals(requesterId, userFriendDto.getRequesterId());
    }

    @Test
    void testUserFriendDtoConstructorWithoutUserLocation() {
        Long id = 1L;
        String name = "name";
        String email = "email";
        Double rating = 1.0;
        Long ulId = null;
        Long mutualFriends = 2L;
        String profilePicturePath = "/path/to/profile/picture";
        Long chatId = 4L;
        String friendStatus = "FRIEND";
        Long requesterId = 3L;

        UserFriendDto userFriendDto = new UserFriendDto(
            id, name, email, rating, ulId, null, null, null, null, null, null,
            null, null, mutualFriends, profilePicturePath, chatId, friendStatus, requesterId);

        assertNull(userFriendDto.getUserLocationDto());
    }

    @Test
    void getSortableFieldsShouldReturnExpectedFieldsTest() {
        UserFriendDto dto = new UserFriendDto();
        List<String> sortableFields = dto.getSortableFields();
        Set<String> expectedFields = Set.of(
            SortableFields.ID.getFieldName(),
            SortableFields.NAME.getFieldName(),
            SortableFields.EMAIL.getFieldName(),
            SortableFields.RATING.getFieldName(),
            SortableFields.MUTUAL_FRIENDS.getFieldName());

        assertEquals(expectedFields, new HashSet<>(sortableFields));
    }
}
