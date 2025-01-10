package greencity.service;

import greencity.ModelUtils;
import greencity.dto.factoftheday.FactOfTheDayTranslationVO;
import greencity.entity.FactOfTheDayTranslation;
import greencity.repository.FactOfTheDayTranslationRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FactOfTheDayTranslationServiceImplTest {

    @Mock
    private FactOfTheDayTranslationRepo factOfTheDayTranslationRepo;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private FactOfTheDayTranslationServiceImpl factOfTheDayTranslationService;

    FactOfTheDayTranslationVO factOfTheDayTranslationVO = ModelUtils.getFactOfTheDayTranslationVO();

    @Test
    void getFactOfTheDayById() {
        Long id = 1L;
        FactOfTheDayTranslation factOfTheDayTranslation = FactOfTheDayTranslation.builder()
            .id(1L)
            .content("Content")
            .language(ModelUtils.getLanguage())
            .factOfTheDay(ModelUtils.getFactOfTheDay())
            .build();
        Optional<FactOfTheDayTranslation> fact = Optional.of(factOfTheDayTranslation);
        Optional<FactOfTheDayTranslationVO> factVO = Optional.of(factOfTheDayTranslationVO);
        when(factOfTheDayTranslationRepo.findById(id)).thenReturn(fact);
        when(modelMapper.map(fact, FactOfTheDayTranslationVO.class)).thenReturn(factOfTheDayTranslationVO);
        assertEquals(factVO, factOfTheDayTranslationService.getFactOfTheDayById(id));
    }

    @Test
    void save() {
        FactOfTheDayTranslation factOfTheDayTranslation = FactOfTheDayTranslation.builder()
            .id(1L)
            .content("Content")
            .language(ModelUtils.getLanguage())
            .factOfTheDay(ModelUtils.getFactOfTheDay())
            .build();
        when(modelMapper.map(factOfTheDayTranslationVO, FactOfTheDayTranslation.class))
            .thenReturn(factOfTheDayTranslation);
        when(factOfTheDayTranslationRepo.save(factOfTheDayTranslation)).thenReturn(factOfTheDayTranslation);
        when(modelMapper.map(factOfTheDayTranslation, FactOfTheDayTranslationVO.class))
            .thenReturn(factOfTheDayTranslationVO);

        assertEquals(factOfTheDayTranslationVO, factOfTheDayTranslationService.save(factOfTheDayTranslationVO));
    }

    @Test
    void saveAll() {
        FactOfTheDayTranslation factOfTheDayTranslation = FactOfTheDayTranslation.builder()
            .id(1L)
            .content("Content")
            .language(ModelUtils.getLanguage())
            .factOfTheDay(ModelUtils.getFactOfTheDay())
            .build();
        List<FactOfTheDayTranslationVO> factOfTheDayTranslationVOList =
            Collections.singletonList(factOfTheDayTranslationVO);
        List<FactOfTheDayTranslation> factOfTheDayTranslationList = Collections.singletonList(factOfTheDayTranslation);

        when(modelMapper.map(factOfTheDayTranslationVO, FactOfTheDayTranslation.class))
            .thenReturn(factOfTheDayTranslation);
        when(factOfTheDayTranslationRepo.saveAll(factOfTheDayTranslationList)).thenReturn(factOfTheDayTranslationList);
        when(modelMapper.map(factOfTheDayTranslationList, new TypeToken<List<FactOfTheDayTranslationVO>>() {
        }.getType())).thenReturn(factOfTheDayTranslationVOList);
        assertEquals(factOfTheDayTranslationVOList,
            factOfTheDayTranslationService.saveAll(factOfTheDayTranslationVOList));
    }

    @Test
    void deleteAll() {
        FactOfTheDayTranslation factOfTheDayTranslation = FactOfTheDayTranslation.builder()
            .id(1L)
            .content("Content")
            .language(ModelUtils.getLanguage())
            .factOfTheDay(ModelUtils.getFactOfTheDay())
            .build();
        List<FactOfTheDayTranslationVO> factOfTheDayTranslationVOList =
            Collections.singletonList(factOfTheDayTranslationVO);
        List<FactOfTheDayTranslation> factOfTheDayTranslationList = Collections.singletonList(factOfTheDayTranslation);
        when(modelMapper.map(factOfTheDayTranslationVO, FactOfTheDayTranslation.class))
            .thenReturn(factOfTheDayTranslation);
        factOfTheDayTranslationService.deleteAll(factOfTheDayTranslationVOList);
        verify(factOfTheDayTranslationRepo).deleteAll(factOfTheDayTranslationList);
    }
}
