package greencity.mapping;

import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievement.UserAchievementVO;
import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.language.LanguageVO;
import greencity.dto.location.UserLocationDto;
import greencity.dto.ownsecurity.OwnSecurityVO;
import greencity.dto.socialnetwork.SocialNetworkImageVO;
import greencity.dto.socialnetwork.SocialNetworkVO;
import greencity.dto.user.UserVO;
import greencity.dto.useraction.UserActionVO;
import greencity.dto.verifyemail.VerifyEmailVO;
import greencity.entity.User;
import greencity.entity.UserLocation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Optional;
import java.util.stream.Collectors;

@Component
public class UserVOMapper extends AbstractConverter<User, UserVO> {
    @Override
    protected UserVO convert(User user) {
        return UserVO.builder()
            .id(user.getId())
            .name(user.getName())
            .email(user.getEmail())
            .role(user.getRole())
            .userCredo(user.getUserCredo())
            .firstName(user.getFirstName())
            .emailNotification(user.getEmailNotification())
            .userStatus(user.getUserStatus())
            .rating(user.getRating())
            .verifyEmail(user.getVerifyEmail() != null ? VerifyEmailVO.builder()
                .id(user.getVerifyEmail().getId())
                .user(UserVO.builder()
                    .id(user.getVerifyEmail().getUser().getId())
                    .name(user.getVerifyEmail().getUser().getName())
                    .build())
                .token(user.getVerifyEmail().getToken())
                .build() : null)
            .userFriends(user.getUserFriends() != null ? user.getUserFriends()
                .stream().map(user1 -> UserVO.builder()
                    .id(user1.getId())
                    .name(user1.getName())
                    .build())
                .collect(Collectors.toList()) : null)
            .refreshTokenKey(user.getRefreshTokenKey())
            .ownSecurity(user.getOwnSecurity() != null ? OwnSecurityVO.builder()
                .id(user.getOwnSecurity().getId())
                .password(user.getOwnSecurity().getPassword())
                .user(UserVO.builder()
                    .id(user.getOwnSecurity().getUser().getId())
                    .email(user.getOwnSecurity().getUser().getEmail())
                    .build())
                .build() : null)
            .dateOfRegistration(user.getDateOfRegistration())
            .userLocationDto(convertUserLocationToDto(user.getUserLocation()))
            .profilePicturePath(user.getProfilePicturePath())
            .showToDoList(user.getShowToDoList())
            .showEcoPlace(user.getShowEcoPlace())
            .showLocation(user.getShowLocation())
            .socialNetworks(user.getSocialNetworks() != null ? user.getSocialNetworks()
                .stream().map(socialNetwork -> SocialNetworkVO.builder()
                    .id(socialNetwork.getId())
                    .url(socialNetwork.getUrl())
                    .user(UserVO.builder()
                        .id(socialNetwork.getUser().getId())
                        .email(socialNetwork.getUser().getEmail())
                        .build())
                    .socialNetworkImage(SocialNetworkImageVO.builder()
                        .id(socialNetwork.getSocialNetworkImage().getId())
                        .imagePath(socialNetwork.getSocialNetworkImage().getImagePath())
                        .hostPath(socialNetwork.getSocialNetworkImage().getHostPath())
                        .build())
                    .build())
                .collect(Collectors.toList()) : new ArrayList<>())
            .lastActivityTime(user.getLastActivityTime())
            .userAchievements(user.getUserAchievements() != null ? user.getUserAchievements()
                .stream().map(userAchievement -> UserAchievementVO.builder()
                    .id(userAchievement.getId())
                    .user(UserVO.builder()
                        .id(userAchievement.getUser().getId())
                        .build())
                    .achievement(AchievementVO.builder()
                        .id(userAchievement.getAchievement().getId())
                        .build())
                    .build())
                .collect(Collectors.toList()) : new ArrayList<>())
            .userActions(user.getUserActions() != null ? user.getUserActions()
                .stream().map(userAction -> UserActionVO.builder()
                    .id(userAction.getId())
                    .achievementCategory(AchievementCategoryVO.builder()
                        .id(userAction.getAchievementCategory().getId())
                        .build())
                    .count(userAction.getCount())
                    .user(UserVO.builder()
                        .id(userAction.getUser().getId())
                        .build())
                    .build())
                .collect(Collectors.toList()) : new ArrayList<>())
            .languageVO(user.getLanguage() != null ? LanguageVO.builder()
                .id(user.getLanguage().getId())
                .code(user.getLanguage().getCode())
                .build() : null)
            .build();
    }

    private UserLocationDto convertUserLocationToDto(UserLocation userLocation) {
        return Optional.ofNullable(userLocation)
            .map(ul -> UserLocationDto.builder()
                .id(ul.getId())
                .cityEn(ul.getCityEn())
                .cityUa(ul.getCityUa())
                .regionEn(ul.getRegionEn())
                .regionUa(ul.getRegionUa())
                .countryEn(ul.getCountryEn())
                .countryUa(ul.getCountryUa())
                .latitude(ul.getLatitude())
                .longitude(ul.getLongitude())
                .build())
            .orElse(null);
    }
}