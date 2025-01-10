package greencity.mapping;

import greencity.dto.user.UserManagementVO;
import greencity.entity.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;

import static greencity.ModelUtils.getUser;
import static greencity.ModelUtils.getUserManagementVO;
import static greencity.ModelUtils.getUserManagementVOPage;
import static greencity.ModelUtils.getUserPage;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(MockitoExtension.class)
class UserManagementVOMapperTest {
    @InjectMocks
    UserManagementVOMapper userManagementVOMapper;

    @Test
    void convertTest() {
        var user = getUser();
        var userManagementVO = getUserManagementVO();
        UserManagementVO result = userManagementVOMapper.convert(user);
        assertEquals(userManagementVO, result);
    }

    @Test
    void mapAllToPageTest() {
        Page<User> userPage = getUserPage();
        Page<UserManagementVO> expected = getUserManagementVOPage();
        Page<UserManagementVO> result = userManagementVOMapper.mapAllToPage(userPage);
        assertEquals(expected.getContent(), result.getContent());
        assertEquals(expected.getTotalElements(), result.getTotalElements());
        assertEquals(expected.getPageable(), result.getPageable());
    }
}
