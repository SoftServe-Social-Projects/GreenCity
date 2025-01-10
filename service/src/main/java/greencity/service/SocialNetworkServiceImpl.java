package greencity.service;

import greencity.dto.socialnetwork.SocialNetworkVO;
import greencity.repository.SocialNetworkRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SocialNetworkServiceImpl implements SocialNetworkService {
    public final SocialNetworkRepo socialNetworkRepo;

    /**
     * {@inheritDoc}
     */
    @Override
    public Long delete(Long userId) {
        socialNetworkRepo.deleteById(userId);
        return userId;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSocialNetworkUrlByName(List<SocialNetworkVO> socialNetworks, String socialNetworkName) {
        return socialNetworks.stream()
            .map(SocialNetworkVO::getUrl)
            .filter(url -> url.contains(socialNetworkName))
            .findFirst()
            .orElse(null);
    }
}