package greencity.service;

import greencity.dto.socialnetwork.SocialNetworkVO;
import java.util.List;

public interface SocialNetworkService {
    /**
     * Method for delete social network.
     *
     * @param id of {@link SocialNetworkVO}
     */
    Long delete(Long id);

    /**
     * Method for getting user's social network url by social network name.
     *
     * @param socialNetworks    - {@link List} of user's {@link SocialNetworkVO}
     *                          instances.
     * @param socialNetworkName - name of {@link SocialNetworkVO}.
     */
    String getSocialNetworkUrlByName(List<SocialNetworkVO> socialNetworks, String socialNetworkName);
}