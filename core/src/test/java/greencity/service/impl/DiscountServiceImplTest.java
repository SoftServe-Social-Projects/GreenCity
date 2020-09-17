package greencity.service.impl;

import greencity.entity.DiscountValue;
import greencity.exception.exceptions.NewsSubscriberPresentException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.DiscountValuesRepo;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DiscountServiceImplTest {
    @Mock
    private DiscountValuesRepo discountValuesRepo;

    @InjectMocks
    private DiscountServiceImpl discountService;

    @Test
    void save() {
        DiscountValue discountValue = new DiscountValue();
        when(discountValuesRepo.save(any(DiscountValue.class))).thenReturn(new DiscountValue());
        assertEquals(discountValue, discountService.save(discountValue));
    }

    @Test
    void findById() {
        DiscountValue genericEntity = new DiscountValue();
        when(discountValuesRepo.findById(anyLong())).thenReturn(Optional.of(genericEntity));
        DiscountValue foundEntity = discountService.findById(anyLong());
        assertEquals(genericEntity, foundEntity);
    }

    @Test
    void findByIdGivenIdNullThenThrowException() {
        Assertions
            .assertThrows(NotFoundException.class,
                () -> discountService.findById(null));
    }

    @Test
    void findAllByPlaceId() {
        Set<DiscountValue> genericSet = new HashSet<>();
        when(discountValuesRepo.findAllByPlaceId(anyLong())).thenReturn(genericSet);
        Set<DiscountValue> foundSet = discountService.findAllByPlaceId(anyLong());
        assertEquals(genericSet, foundSet);
    }
}