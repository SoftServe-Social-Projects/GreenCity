package greencity.service.impl;

import greencity.constant.CacheConstants;
import greencity.dto.achievement.AchievementDTO;
import greencity.repository.AchievementRepo;
import greencity.service.AchievementService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class AchievementServiceImpl implements AchievementService {
    private AchievementRepo achievementRepo;
    private ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     *
     * @author Yuriy Olkhovskyi
     */
    @Cacheable(value = CacheConstants.ALL_ACHIEVEMENTS_CACHE_NAME)
    @Override
    public List<AchievementDTO> findAll() {
        return achievementRepo.findAll()
            .stream()
            .map(achieve -> modelMapper.map(achieve, AchievementDTO.class))
            .collect(Collectors.toList());
    }
}
