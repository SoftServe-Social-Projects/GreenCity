package greencity.dto.user;

import greencity.annotations.ValidSocialNetworkLinks;
import java.util.List;

import greencity.enums.EcoPlacePrivacyPolicy;
import greencity.enums.LocationPrivacyPolicy;
import greencity.enums.ToDoListPrivacyPolicy;
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
    private LocationPrivacyPolicy showLocation;
    private EcoPlacePrivacyPolicy showEcoPlace;
    private ToDoListPrivacyPolicy showShoppingList;
}
