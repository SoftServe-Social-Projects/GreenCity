package greencity.repository;

import greencity.entity.User;
import greencity.entity.UserAction;
import greencity.enums.ActionContextType;
import greencity.enums.UserActionType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserActionRepo extends JpaRepository<UserAction, Long> {
    /**
     * Finds out if action is already logged.
     *
     * @param userId      id of {@link User}.
     * @param actionType  {@link UserActionType} of action.
     * @param contextType {@link ActionContextType} of action.
     * @param contextId   {@link Long} of action.
     * @return {@code true} if action is logged, {@code false} otherwise.
     */
    boolean existsByUserIdAndActionTypeAndContextTypeAndContextId(
        Long userId, UserActionType actionType, ActionContextType contextType, Long contextId);

    /**
     * Counts all actions of user by given action type.
     *
     * @param userId     id of {@link User} whose actions are to be counted.
     * @param actionType {@link UserActionType} type of actions.
     * @return {@link Long} - number of actions.
     */
    Long countAllByUserIdAndActionType(Long userId, UserActionType actionType);
}
