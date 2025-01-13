package greencity.mapping;

import greencity.dto.user.UserManagementVO;
import greencity.entity.User;
import org.modelmapper.AbstractConverter;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

@Component
public class UserManagementVOMapper extends AbstractConverter<User, UserManagementVO> {
    @Override
    protected UserManagementVO convert(User user) {
        return UserManagementVO.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .userCredo(user.getUserCredo())
            .role(user.getRole())
            .userStatus(user.getUserStatus())
            .build();
    }

    /**
     * Method to convert {@link Page} of {@link User} to {@link Page} of
     * {@link UserManagementVO}.
     *
     * @param users {@link Page} of {@link User}
     * @return {@link Page} of {@link UserManagementVO}
     * @author Anton Bondar
     */
    public Page<UserManagementVO> mapAllToPage(Page<User> users) {
        return users.map(this::convert);
    }
}
