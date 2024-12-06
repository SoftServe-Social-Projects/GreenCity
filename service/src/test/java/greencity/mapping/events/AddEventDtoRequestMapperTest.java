package greencity.mapping.events;

import greencity.ModelUtils;
import greencity.dto.event.AddEventDtoRequest;
import greencity.entity.event.Event;
import greencity.exception.exceptions.BadRequestException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(SpringExtension.class)
class AddEventDtoRequestMapperTest {
    @Mock
    private AddressDtoMapper addressDtoMapper;
    @InjectMocks
    private AddEventDtoRequestMapper mapper;

    @Test
    void convertTestWithoutAddress() {
        Event expected = ModelUtils.getEventWithoutAddress();
        AddEventDtoRequest request = ModelUtils.addEventDtoWithoutAddressRequest;

        assertEquals(expected.getTitle(), mapper.convert(request).getTitle());
    }

    @Test
    void convertTestWithoutOnlineLink() {
        Event expected = ModelUtils.getEvent();
        AddEventDtoRequest request = ModelUtils.addEventDtoWithoutLinkRequest;

        assertEquals(expected.getTitle(), mapper.convert(request).getTitle());
    }

    @Test
    void convertTestWithoutAddressAndOnlineLink() {
        AddEventDtoRequest request = ModelUtils.addEventDtoWithoutAddressAndLinkRequest;
        assertThrows(BadRequestException.class, () -> mapper.convert(request));
    }

    @Test
    void convertTestWithNullRegionUa() {
        AddEventDtoRequest request = ModelUtils.addEventDtoRequestWithNullRegionUa;
        assertThrows(BadRequestException.class, () -> mapper.convert(request));
    }

    @Test
    void convertTestWithNullCountryUa() {
        AddEventDtoRequest request = ModelUtils.addEventDtoRequestWithNullCountryUa;
        assertThrows(BadRequestException.class, () -> mapper.convert(request));
    }

    @Test
    void convertTestWhenAddressDtoWithNullData() {
        AddEventDtoRequest request = ModelUtils.addEventDtoRequestWithNullData;
        assertThrows(BadRequestException.class, () -> mapper.convert(request));
    }
}
