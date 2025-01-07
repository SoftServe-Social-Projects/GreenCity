package greencity.dto.user;

import greencity.annotations.ValidSocialNetworkLinks;
import java.util.List;

import greencity.enums.ProfilePrivacyPolicy;
import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserProfileDtoRequest {
    private String name;
    private String city;
    private String userCredo;
    @ValidSocialNetworkLinks
    private List<String> socialNetworks;
    private ProfilePrivacyPolicy showLocation;
    private ProfilePrivacyPolicy showEcoPlace;
    private ProfilePrivacyPolicy showShoppingList;
}
