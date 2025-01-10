package greencity.repository.impl;

import greencity.ModelUtils;
import greencity.dto.friends.UserFriendDto;
import greencity.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomUserRepoImplTest {
    @InjectMocks
    private CustomUserRepoImpl customUserRepo;

    @Mock
    private EntityManager entityManager;

    @Test
    void fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUserTest() {
        long userId = 1L;
        List<User> users = List.of(ModelUtils.getUser());
        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());
        TypedQuery<UserFriendDto> query = mock(TypedQuery.class);

        when(entityManager.createNamedQuery("User.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser",
            UserFriendDto.class)).thenReturn(query);

        customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, users);

        verify(query).setParameter("userId", userId);
        verify(query).setParameter("users", userIds);
    }

    @Test
    void fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUserWhenUserListIsEmptyTest() {
        long userId = 1L;
        List<User> users = List.of();
        TypedQuery<UserFriendDto> query = mock(TypedQuery.class);

        when(entityManager.createNamedQuery("User.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser",
            UserFriendDto.class)).thenReturn(query);

        customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, users);

        verify(query).setParameter("userId", userId);
        verify(query).setParameter("users", Collections.singletonList(-1L));
    }

    @Test
    void fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUserWhenUserListIsNull() {
        long userId = 1L;

        assertThrows(NullPointerException.class,
            () -> customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, null));
    }
}
