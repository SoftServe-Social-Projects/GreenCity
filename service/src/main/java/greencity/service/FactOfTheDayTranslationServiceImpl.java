package greencity.service;

import greencity.dto.factoftheday.FactOfTheDayTranslationVO;
import greencity.entity.FactOfTheDayTranslation;
import greencity.repository.FactOfTheDayTranslationRepo;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Implementation of {@link FactOfTheDayTranslationService}.
 *
 * @author Mykola Lehkyi
 */
@AllArgsConstructor
@Service
public class FactOfTheDayTranslationServiceImpl implements FactOfTheDayTranslationService {
    private final FactOfTheDayTranslationRepo factOfTheDayTranslationRepo;
    private final ModelMapper modelMapper;

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<FactOfTheDayTranslationVO> getFactOfTheDayById(Long id) {
        return Optional.of(modelMapper.map(factOfTheDayTranslationRepo.findById(id), FactOfTheDayTranslationVO.class));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public FactOfTheDayTranslationVO save(FactOfTheDayTranslationVO factOfTheDayTranslation) {
        FactOfTheDayTranslation map = modelMapper.map(factOfTheDayTranslation, FactOfTheDayTranslation.class);
        return modelMapper.map(factOfTheDayTranslationRepo.save(map), FactOfTheDayTranslationVO.class);
    }

    @Override
    public List<FactOfTheDayTranslationVO> saveAll(List<FactOfTheDayTranslationVO> factOfTheDayTranslation) {
        List<FactOfTheDayTranslation> collect = factOfTheDayTranslation.stream()
            .map(fact -> modelMapper.map(fact, FactOfTheDayTranslation.class))
            .collect(Collectors.toList());
        return modelMapper.map(factOfTheDayTranslationRepo.saveAll(collect),
            new TypeToken<List<FactOfTheDayTranslationVO>>() {
            }.getType());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAll(List<FactOfTheDayTranslationVO> factOfTheDayTranslations) {
        List<FactOfTheDayTranslation> collect = factOfTheDayTranslations.stream()
            .map(fact -> modelMapper.map(fact, FactOfTheDayTranslation.class))
            .collect(Collectors.toList());
        factOfTheDayTranslationRepo.deleteAll(collect);
    }
}
