package greencity.dto.user;

import greencity.dto.achievement.UserAchievementVO;
import greencity.dto.econews.EcoNewsVO;
import greencity.dto.econewscomment.EcoNewsCommentVO;
import greencity.dto.language.LanguageVO;
import greencity.dto.location.UserLocationDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemVO;
import greencity.dto.ownsecurity.OwnSecurityVO;
import greencity.dto.socialnetwork.SocialNetworkVO;
import greencity.dto.useraction.UserActionVO;
import greencity.dto.verifyemail.VerifyEmailVO;
import greencity.enums.EmailNotification;
import greencity.enums.ProfilePrivacyPolicy;
import greencity.enums.Role;
import greencity.enums.UserStatus;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class UserVO {
    private Long id;

    private String name;

    private String email;

    private Role role;

    private String userCredo;

    private UserStatus userStatus;

    private List<UserShoppingListItemVO> userShoppingListItemVOS = new ArrayList<>();

    private List<CustomShoppingListItemVO> customShoppingListItemVOS = new ArrayList<>();

    private VerifyEmailVO verifyEmail;

    private Double rating;

    private EmailNotification emailNotification;

    private LocalDateTime dateOfRegistration;

    private List<SocialNetworkVO> socialNetworks;

    private List<UserVO> userFriends = new ArrayList<>();

    private List<UserAchievementVO> userAchievements = new ArrayList<>();

    private String refreshTokenKey;

    private OwnSecurityVO ownSecurity;

    private String profilePicturePath;

    private Set<EcoNewsVO> ecoNewsLiked;

    private Set<EcoNewsCommentVO> ecoNewsCommentsLiked;

    private String firstName;

    private ProfilePrivacyPolicy showLocation;

    private ProfilePrivacyPolicy showEcoPlace;

    private ProfilePrivacyPolicy showShoppingList;

    private LocalDateTime lastActivityTime;

    private List<UserActionVO> userActions = new ArrayList<>();

    private LanguageVO languageVO;

    private UserLocationDto userLocationDto;
}
