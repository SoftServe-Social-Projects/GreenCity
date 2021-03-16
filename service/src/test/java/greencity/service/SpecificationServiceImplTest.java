package greencity.service;

import greencity.dto.specification.SpecificationVO;
import greencity.entity.Specification;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.SpecificationRepo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(SpringExtension.class)
class SpecificationServiceImplTest {
    @Mock
    private SpecificationRepo specificationRepo;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SpecificationServiceImpl specificationService;

    SpecificationVO specificationVO = new SpecificationVO(1L, "specification");

    @Test
    void saveTest() {
        Specification genericEntity = new Specification();

        when(specificationRepo.save(genericEntity)).thenReturn(genericEntity);

        assertEquals(modelMapper.map(genericEntity, SpecificationVO.class), specificationService.save(specificationVO));
    }

    @Test
    void findByIdTest() {
        Specification genericEntity = new Specification();

        when(specificationRepo.findById(anyLong())).thenReturn(Optional.of(genericEntity));

        SpecificationVO specificationVO = specificationService.findById(anyLong());

        assertEquals(modelMapper.map(genericEntity, SpecificationVO.class), specificationVO);
    }

    @Test
    void findByIdGivenIdNullThenThrowException() {
        assertThrows(NotFoundException.class, () -> {
            specificationService.findById(null);
        });
    }

    @Test
    void findAllTest() {
        List<Specification> expected = new ArrayList<>(Arrays.asList(
            new Specification(1L, "spec", null)));
        List<SpecificationVO> expectedVO = Arrays.asList(new SpecificationVO(1L, "spec"));

        when(specificationRepo.findAll()).thenReturn(expected);
        when(modelMapper.map(expected, new TypeToken<List<SpecificationVO>>() {
        }.getType())).thenReturn(expectedVO);

        assertEquals(expectedVO, specificationService.findAll());
    }

    @Test
    void deleteByIdTest() {
        when(specificationRepo.findById(anyLong())).thenReturn(Optional.of(new Specification()));

        assertEquals(new Long(1), specificationService.deleteById(1L));
    }

    @Test
    void deleteByIdGivenIdNullThenThrowException() {
        assertThrows(NotFoundException.class, () -> {
            specificationService.deleteById(null);
        });
    }

    @Test
    void findByNameTest() {
        Specification genericEntity = new Specification();

        when(specificationRepo.findByName(anyString())).thenReturn(Optional.of(genericEntity));

        SpecificationVO specificationVO = specificationService.findByName(anyString());

        assertEquals(modelMapper.map(genericEntity, SpecificationVO.class), specificationVO);
    }
}
