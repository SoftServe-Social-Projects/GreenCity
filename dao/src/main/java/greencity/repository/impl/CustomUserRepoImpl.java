package greencity.repository.impl;

import greencity.dto.friends.UserFriendDto;
import greencity.entity.User;
import greencity.repository.CustomUserRepo;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.TypedQuery;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Repository
public class CustomUserRepoImpl implements CustomUserRepo {
    private final EntityManager entityManager;

    /**
     * {@inheritDoc}
     */
    @Override
    public List<UserFriendDto> fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser(long userId,
        List<User> users) {
        Objects.requireNonNull(users);

        TypedQuery<UserFriendDto> query = entityManager
            .createNamedQuery("User.fillListOfUserWithCountOfMutualFriendsAndChatIdForCurrentUser",
                UserFriendDto.class);
        query.setParameter("userId", userId);

        List<Long> userIds;
        if (users.isEmpty()) {
            userIds = Collections.singletonList(-1L);
        } else {
            userIds = users.stream().map(User::getId).collect(Collectors.toList());
        }
        query.setParameter("users", userIds);

        List<UserFriendDto> resultList = query.getResultList();
        Map<Long, UserFriendDto> resultMap = resultList.stream()
            .collect(Collectors.toMap(UserFriendDto::getId, dto -> dto));

        return userIds.stream()
            .map(resultMap::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }
}
