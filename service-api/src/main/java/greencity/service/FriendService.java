package greencity.service;

import greencity.dto.PageableDto;
import greencity.dto.friends.UserAsFriendDto;
import greencity.dto.friends.UserFriendDto;
import greencity.dto.user.UserManagementDto;
import greencity.dto.user.UserVO;
import greencity.enums.RecommendedFriendsType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface FriendService {
    /**
     * Delete user's friend by friendId.
     *
     * @param userId   user id
     * @param friendId friend id
     * @author Marian Datsko
     */
    void deleteUserFriendById(long userId, long friendId);

    /**
     * Add new friend by friendId.
     *
     * @param userId   user id
     * @param friendId friend id
     * @author Marian Datsko
     */
    void addNewFriend(long userId, long friendId);

    /**
     * Accept friend request {@link UserVO}.
     *
     * @param userId   user id
     * @param friendId friend id
     */
    void acceptFriendRequest(long userId, long friendId);

    /**
     * Decline friend request {@link UserVO}.
     *
     * @param userId   user id
     * @param friendId friend id
     */
    void declineFriendRequest(long userId, long friendId);

    /**
     * Method that finds user's friends by userId.
     *
     * @param userId user id
     *
     * @return {@link Page} of {@link UserManagementDto} instances.
     */
    PageableDto<UserManagementDto> findUserFriendsByUserId(Pageable pageable, long userId);

    /**
     * Method that finds user's friends by userId but setting FriendStatus related
     * to current user. Friends order: friends, who are tracking the same habits as
     * user with userId; friends, who live in the same city as user with userId;
     * friends, who have the highest personal rate.
     *
     * @param userId        The ID of the user.
     * @param currentUserId The ID of the current user.
     *
     * @return {@link Page} of {@link UserFriendDto} instances.
     *
     * @author Lilia Mokhnatska
     */
    PageableDto<UserFriendDto> findUserFriendsByUserIAndShowFriendStatusRelatedToCurrentUser(Pageable pageable,
        long userId,
        long currentUserId);

    /**
     * Method find all users except current user and his friends.
     *
     * @param userId   user id.
     * @param name     filtering name.
     * @param pageable the information about pagination and sorting for the result,
     *                 must not be null.
     *
     * @return {@link PageableDto} of {@link UserFriendDto}.
     *
     * @author Stepan Omeliukh
     */
    PageableDto<UserFriendDto> findAllUsersExceptMainUserAndUsersFriendAndRequestersToMainUser(long userId,
        String name,
        boolean filterByFriendsOfFriends,
        boolean filterByCity,
        Pageable pageable);

    /**
     * Method to find {@link UserFriendDto}s which sent request to user with userId.
     *
     * @param pageable the information about pagination and sorting for the result,
     *                 must not be null.
     * @param userId   user id.
     *
     * @return {@link PageableDto} of {@link UserFriendDto}.
     */
    PageableDto<UserFriendDto> getAllUserFriendRequests(long userId, String name, boolean filterByCity,
        Pageable pageable);

    /**
     * Method that finds all user's friends.
     *
     * @param userId   user id.
     * @param name     filtering name.
     * @param pageable pageable, must not be null.
     *
     * @return {@link PageableDto} of {@link UserFriendDto} instances.
     */
    PageableDto<UserFriendDto> findAllFriendsOfUser(
        long userId,
        String name,
        boolean filterByCity,
        Pageable pageable);

    /**
     * Method find recommended friends for user by recommendation type.
     *
     * @param userId   user id.
     * @param type     type to find recommended friends
     * @param pageable the information about pagination and sorting for the result,
     *                 must not be null.
     *
     * @return {@link PageableDto} of {@link UserFriendDto}.
     */
    PageableDto<UserFriendDto> findRecommendedFriends(long userId, RecommendedFriendsType type, Pageable pageable);

    /**
     * Method that finds mutual friends for user.
     *
     * @param userId   user id.
     * @param friendId friend id.
     * @param pageable the information about pagination and sorting for the result,
     *                 must not be null.
     *
     * @return {@link PageableDto} of {@link UserFriendDto}.
     */
    PageableDto<UserFriendDto> getMutualFriends(Long userId, Long friendId, Pageable pageable);

    /**
     * Delete user's request to be a friend by friendId.
     *
     * @param userId   user id
     * @param friendId friend id
     * @author Marian Datsko
     */
    void deleteRequestOfCurrentUserToFriend(long userId, long friendId);

    /**
     * Get user data as friend.
     *
     * @param currentUserId user id
     * @param friendId      friend id
     * @author Denys Ryhal
     */
    UserAsFriendDto getUserAsFriend(Long currentUserId, Long friendId);
}
