package greencity.dto.user;

import greencity.dto.achievement.UserAchievementVO;
import greencity.dto.econews.EcoNewsVO;
import greencity.dto.language.LanguageVO;
import greencity.dto.location.UserLocationDto;
import greencity.dto.ownsecurity.OwnSecurityVO;
import greencity.dto.todolistitem.CustomToDoListItemVO;
import greencity.dto.socialnetwork.SocialNetworkVO;
import greencity.dto.useraction.UserActionVO;
import greencity.dto.verifyemail.VerifyEmailVO;
import greencity.enums.EmailNotification;
import greencity.enums.ProfilePrivacyPolicy;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
public class UserVO {
    private Long id;

    private String name;

    private String email;

    private Role role;

    private String userCredo;

    private UserStatus userStatus;

    @Builder.Default
    private List<UserToDoListItemVO> userToDoListItemVOS = new ArrayList<>();

    @Builder.Default
    private List<CustomToDoListItemVO> customToDoListItemVOS = new ArrayList<>();

    private VerifyEmailVO verifyEmail;

    private Double rating;

    private EmailNotification emailNotification;

    private LocalDateTime dateOfRegistration;

    private List<SocialNetworkVO> socialNetworks;

    @Builder.Default
    private List<UserVO> userFriends = new ArrayList<>();

    @Builder.Default
    private List<UserAchievementVO> userAchievements = new ArrayList<>();

    private String refreshTokenKey;

    private OwnSecurityVO ownSecurity;

    private String profilePicturePath;

    private Set<EcoNewsVO> ecoNewsLiked;

    private String firstName;

    private ProfilePrivacyPolicy showLocation;

    private ProfilePrivacyPolicy showEcoPlace;

    private ProfilePrivacyPolicy showToDoList;

    private LocalDateTime lastActivityTime;

    @Builder.Default
    private List<UserActionVO> userActions = new ArrayList<>();

    private LanguageVO languageVO;

    private UserLocationDto userLocationDto;
}
