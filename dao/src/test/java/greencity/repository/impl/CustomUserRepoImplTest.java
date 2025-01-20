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

import static org.junit.jupiter.api.Assertions.assertEquals;
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

    @Test
    void fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUserPreservesOrderTest() {
        long userId = 1L;
        long user1Id = 10L;
        long user2Id = 20L;
        long user3Id = 30L;

        User user1 = ModelUtils.getUser();
        user1.setId(user1Id);
        User user2 = ModelUtils.getUser();
        user2.setId(user2Id);
        User user3 = ModelUtils.getUser();
        user3.setId(user3Id);
        List<User> users = List.of(user1, user2, user3);

        TypedQuery<UserFriendDto> query = mock(TypedQuery.class);
        when(entityManager.createNamedQuery("User.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser",
            UserFriendDto.class)).thenReturn(query);

        UserFriendDto friend1 = new UserFriendDto();
        friend1.setId(user2Id);
        UserFriendDto friend2 = new UserFriendDto();
        friend2.setId(user3Id);
        UserFriendDto friend3 = new UserFriendDto();
        friend3.setId(user1Id);
        when(query.getResultList()).thenReturn(List.of(friend1, friend2, friend3));

        List<UserFriendDto> result = customUserRepo.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(userId, users);

        verify(query).setParameter("userId", userId);
        verify(query).setParameter("users", List.of(10L, 20L, 30L));

        assertEquals(3, result.size());
        assertEquals(10L, result.get(0).getId());
        assertEquals(20L, result.get(1).getId());
        assertEquals(30L, result.get(2).getId());
    }
}
