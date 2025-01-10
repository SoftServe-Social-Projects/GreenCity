package greencity.service;

import greencity.dto.discount.DiscountValueDto;
import greencity.dto.discount.DiscountValueVO;
import greencity.dto.specification.SpecificationNameDto;
import greencity.dto.specification.SpecificationVO;
import greencity.entity.DiscountValue;
import greencity.entity.Place;
import greencity.entity.Specification;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.modelmapper.ModelMapper;
import org.modelmapper.TypeToken;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PlaceServiceImplPrivateMethodsTet {

    @InjectMocks
    private PlaceServiceImpl placeServiceImpl;

    @Mock
    private DiscountService discountService;

    @Mock
    private SpecificationService specificationService;

    @Mock
    private ModelMapper modelMapper;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void updateDiscountViaReflectionTest() throws Exception {
        Place updatedPlace = new Place();
        updatedPlace.setId(1L);

        SpecificationNameDto specificationNameDto = new SpecificationNameDto();
        specificationNameDto.setName("TestSpecification");

        DiscountValueDto discountValueDto = Mockito.mock(DiscountValueDto.class);
        Mockito.when(discountValueDto.getSpecification()).thenReturn(specificationNameDto);

        Set<DiscountValueDto> discounts = Set.of(discountValueDto);

        SpecificationVO specificationVO = SpecificationVO.builder().build();
        Specification specification = Specification.builder().name("TestSpecification").build();

        DiscountValueVO discountValueVO = new DiscountValueVO();
        DiscountValue discountValue = new DiscountValue();

        Mockito.when(discountService.findAllByPlaceId(Mockito.anyLong()))
            .thenReturn(new HashSet<>(Set.of(discountValueVO)));

        TypeToken<Set<DiscountValue>> typeToken = new TypeToken<>() {
        };
        Mockito.when(modelMapper.map(Mockito.anySet(), Mockito.eq(typeToken.getType())))
            .thenReturn(new HashSet<>(Set.of(discountValue)));

        Mockito.when(specificationService.findByName("TestSpecification")).thenReturn(specificationVO);
        Mockito.when(modelMapper.map(Mockito.any(SpecificationVO.class), Mockito.eq(Specification.class)))
            .thenReturn(specification);
        Mockito.when(modelMapper.map(Mockito.any(DiscountValueDto.class), Mockito.eq(DiscountValue.class)))
            .thenReturn(discountValue);
        Mockito.when(modelMapper.map(Mockito.any(DiscountValue.class), Mockito.eq(DiscountValueVO.class)))
            .thenReturn(discountValueVO);

        Method method = PlaceServiceImpl.class.getDeclaredMethod("updateDiscount", Set.class, Place.class);
        method.setAccessible(true);

        assertDoesNotThrow(() -> method.invoke(placeServiceImpl, discounts, updatedPlace));

        Mockito.verify(discountService).deleteAllByPlaceId(updatedPlace.getId());
        Mockito.verify(discountService).save(discountValueVO);
        Mockito.verify(specificationService).findByName("TestSpecification");
        Mockito.verify(modelMapper, Mockito.times(3)).map(Mockito.any(), Mockito.any());
    }

    @Test
    void updateDiscountHandlesDiscountsCorrectlyTest() {
        Place updatedPlace = new Place();
        updatedPlace.setId(1L);

        SpecificationNameDto specificationNameDto = new SpecificationNameDto();
        specificationNameDto.setName("TestSpecification");

        DiscountValueDto discountValueDto = new DiscountValueDto();
        discountValueDto.setSpecification(specificationNameDto);

        Set<DiscountValueDto> discounts = Set.of(discountValueDto);

        SpecificationVO specificationVO = SpecificationVO.builder().name("TestSpecification").build();
        Specification specification = Specification.builder().name("TestSpecification").build();

        DiscountValue discountValue = new DiscountValue();
        DiscountValueVO discountValueVO = new DiscountValueVO();

        Mockito.when(specificationService.findByName("TestSpecification")).thenReturn(specificationVO);
        Mockito.when(modelMapper.map(Mockito.eq(specificationVO), Mockito.eq(Specification.class)))
            .thenReturn(specification);
        Mockito.when(modelMapper.map(Mockito.eq(discountValueDto), Mockito.eq(DiscountValue.class)))
            .thenReturn(discountValue);
        Mockito.when(modelMapper.map(Mockito.eq(discountValue), Mockito.eq(DiscountValueVO.class)))
            .thenReturn(discountValueVO);
        Set<DiscountValue> newDiscounts = new HashSet<>();
        discounts.forEach(d -> {
            DiscountValue discount = modelMapper.map(d, DiscountValue.class);
            discount.setSpecification(
                modelMapper.map(specificationService.findByName(d.getSpecification().getName()), Specification.class));
            discount.setPlace(updatedPlace);
            discountService.save(modelMapper.map(discount, DiscountValueVO.class));
            newDiscounts.add(discount);
        });
        assertEquals(1, newDiscounts.size(), "New discounts set should contain one element.");
        Mockito.verify(specificationService).findByName("TestSpecification");
        Mockito.verify(modelMapper).map(Mockito.eq(specificationVO), Mockito.eq(Specification.class));
        Mockito.verify(modelMapper).map(Mockito.eq(discountValueDto), Mockito.eq(DiscountValue.class));
        Mockito.verify(modelMapper).map(Mockito.eq(discountValue), Mockito.eq(DiscountValueVO.class));
        Mockito.verify(discountService).save(Mockito.eq(discountValueVO));
    }
}
