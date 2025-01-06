package greencity.dto.user;

import greencity.dto.socialnetwork.SocialNetworkResponseDTO;
import java.util.List;

import greencity.enums.EcoPlacePrivacyPolicy;
import greencity.enums.LocationPrivacyPolicy;
import greencity.enums.Role;
import greencity.enums.ToDoListPrivacyPolicy;
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
    private LocationPrivacyPolicy showLocation;
    private EcoPlacePrivacyPolicy showEcoPlace;
    private ToDoListPrivacyPolicy showShoppingList;
    private Float rating;
    private Role role;
}
