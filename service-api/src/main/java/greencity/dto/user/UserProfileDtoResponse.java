package greencity.dto.user;

import greencity.dto.socialnetwork.SocialNetworkResponseDTO;
import java.util.List;

import greencity.enums.ProfilePrivacyPolicy;
import greencity.enums.Role;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserProfileDtoResponse {
    private String profilePicturePath;
    private String name;
    private String city;
    private String userCredo;
    private List<SocialNetworkResponseDTO> socialNetworks;
    private ProfilePrivacyPolicy showLocation;
    private ProfilePrivacyPolicy showEcoPlace;
    private ProfilePrivacyPolicy showShoppingList;
    private Float rating;
    private Role role;
}
