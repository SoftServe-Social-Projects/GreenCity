package greencity.service;

import greencity.dto.PageableDetailedDto;
import greencity.dto.user.UserFilterDto;
import greencity.dto.user.UserManagementVO;
import greencity.dto.user.UserRoleDto;
import greencity.dto.user.UserStatusDto;
import greencity.dto.user.UserVO;
import greencity.enums.EmailPreference;
import greencity.enums.EmailPreferencePeriodicity;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import org.springframework.data.domain.Pageable;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public interface UserService {
    /**
     * Saves the provided UserVO object into the repository after mapping it to a
     * User entity. This method uses ModelMapper to convert the UserVO object into a
     * User entity and then saves it using the User repository.
     *
     * @param userVO The user value object containing the user details to be saved.
     * @see UserVO
     */
    void save(UserVO userVO);

    /**
     * Method that allow you to find not 'DEACTIVATED' {@link UserVO} by email.
     *
     * @param email - {@link UserVO}'s email
     * @return {@link Optional} of found {@link UserVO}.
     */
    Optional<UserVO> findNotDeactivatedByEmail(String email);

    /**
     * Find UserVO's id by UserVO email.
     *
     * @param email - {@link UserVO} email
     * @return {@link UserVO} id
     */
    Long findIdByEmail(String email);

    /**
     * Updates last activity time for a given user.
     *
     * @param userId               - {@link UserVO}'s id
     * @param userLastActivityTime - new {@link UserVO}'s last activity time
     */
    void updateUserLastActivityTime(Long userId, Date userLastActivityTime);

    /**
     * Method that allow you to find {@link UserVO} by id.
     *
     * @param id a value of {@link Long}
     * @return {@link UserVO} with this id.
     */
    UserVO findById(Long id);

    /**
     * Method that allow you to find {@link UserVO} by email.
     *
     * @param email a value of {@link String}
     * @return {@link UserVO} with this email.
     */
    UserVO findByEmail(String email);

    /**
     * Update status of user.
     *
     * @param id         {@link UserVO} id.
     * @param userStatus {@link UserStatus} for user.
     * @return {@link UserStatusDto}
     */
    UserStatusDto updateStatus(Long id, UserStatus userStatus, String email);

    /**
     * Update {@code ROLE} of user.
     *
     * @param id   {@link UserVO} id.
     * @param role {@link Role} for user.
     * @return {@link UserRoleDto}
     * @deprecated updates like this on User entity should be handled in
     *             GreenCityUser via RestClient.
     */
    @Deprecated
    UserRoleDto updateRole(Long id, Role role, String email);

    /**
     * The method checks by id if a {@link UserVO} is online.
     *
     * @param userId - {@link UserVO}'s id
     * @return {boolean} is user online
     */
    boolean checkIfTheUserIsOnline(Long userId);

    /**
     * Method that returns {@link String} initials (first one or two letters of
     * name) of user.
     *
     * @param userId - {@link UserVO}'s id
     * @return {@link String} user's initials
     */
    String getInitialsById(Long userId);

    /**
     * Method that returns {@link List} of top 6 friends with highest rating.
     *
     * @param userId - {@link UserVO}'s id
     * @return {@link List} of {@link UserVO} instances
     */
    List<UserVO> getSixFriendsWithTheHighestRating(Long userId);

    /**
     * Method for updating user event organizer rating.
     *
     * @param userId - {@link UserVO}'s id
     * @param rate   - new user event organizer rating
     *
     */
    void updateEventOrganizerRating(Long userId, Double rate);

    /**
     * Method that returns a paginated list of users filtered by specified criteria.
     *
     * @param request  request for searching related data
     * @param pageable pagination information including page number, size, and
     *                 sorting options.
     *
     * @return a {@link PageableDetailedDto} containing a list of
     *         {@link UserManagementVO} filtered by the given criteria, role, and
     *         status, along with pagination details.
     *
     * @author Anton Bondar
     */
    PageableDetailedDto<UserManagementVO> getAllUsersByCriteria(UserFilterDto request, Pageable pageable);

    /**
     * Method that update user's rating.
     *
     * @param userId current user's id.
     * @param rating rating.
     */
    void updateUserRating(Long userId, Double rating);

    /**
     * Find list of {@link UserVO}'s by emails.
     *
     * @param emails user emails.
     * @return list of {@link UserVO}.
     */
    List<UserVO> findByEmails(List<String> emails);

    /**
     * Find list of user ids by emailPreference and periodicity.
     *
     * @param emailPreference of user.
     * @param periodicity     of notification.
     * @return list of {@link UserVO}.
     */
    List<UserVO> getUsersIdByEmailPreferenceAndEmailPeriodicity(EmailPreference emailPreference,
        EmailPreferencePeriodicity periodicity);
}