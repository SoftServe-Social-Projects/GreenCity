package greencity.service;

import greencity.ModelUtils;
import greencity.dto.user.UserFilterDtoRequest;
import greencity.dto.user.UserFilterDtoResponse;
import greencity.entity.Filter;
import greencity.entity.User;
import greencity.repository.FilterRepo;
import greencity.repository.UserRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FilterServiceImplTest {
    @Mock
    private FilterRepo filterRepo;

    @Mock
    private UserRepo userRepo;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private FilterServiceImpl filterServiceImpl;

    @Test
    void saveTest() {
        UserFilterDtoRequest dto = ModelUtils.getUserFilterDtoRequest();
        Filter filter = ModelUtils.getFilter();
        User user = ModelUtils.getUser();
        when(modelMapper.map(dto, Filter.class)).thenReturn(filter);
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(filterRepo.save(filter)).thenReturn(filter);
        when(modelMapper.map(filter, UserFilterDtoResponse.class)).thenReturn(ModelUtils.getUserFilterDtoResponse());

        UserFilterDtoResponse dtoResponse = filterServiceImpl.save(1L, dto);

        assertEquals(dtoResponse, ModelUtils.getUserFilterDtoResponse());

        verify(modelMapper).map(dto, Filter.class);
        verify(userRepo).findById(1L);
        verify(filterRepo).save(filter);
        verify(modelMapper).map(filter, UserFilterDtoResponse.class);
    }

    @Test
    void saveTestThreeFilters() {
        UserFilterDtoRequest dto = ModelUtils.getUserFilterDtoRequest();
        Filter filter = ModelUtils.getFilter();
        when(filterRepo.getAllFilters(1L)).thenReturn(List.of(filter, filter, filter));
        when(modelMapper.map(filter, UserFilterDtoResponse.class)).thenReturn(ModelUtils.getUserFilterDtoResponse());

        UserFilterDtoResponse dtoResponse = filterServiceImpl.save(1L, dto);

        assertEquals(dtoResponse, ModelUtils.getUserFilterDtoResponse());

        verify(filterRepo).getAllFilters(anyLong());
        verify(modelMapper).map(filter, UserFilterDtoResponse.class);
    }

    @Test
    void getAllFiltersTest() {
        Filter filter = ModelUtils.getFilter();
        List<Filter> filters = new ArrayList<>();
        filters.add(filter);

        when(filterRepo.getAllFilters(anyLong())).thenReturn(filters);
        when(modelMapper.map(filter, UserFilterDtoResponse.class)).thenReturn(ModelUtils.getUserFilterDtoResponse());

        filterServiceImpl.getAllFilters(anyLong());
        verify(filterRepo).getAllFilters(anyLong());
        verify(modelMapper).map(filter, UserFilterDtoResponse.class);
    }

    @Test
    void getFilterByIdTest() {
        Filter filter = ModelUtils.getFilter();
        when(filterRepo.findById(1L)).thenReturn(Optional.of(filter));
        when(modelMapper.map(filter, UserFilterDtoResponse.class)).thenReturn(ModelUtils.getUserFilterDtoResponse());

        filterServiceImpl.getFilterById(filter.getId());
        verify(filterRepo, times(1)).findById(1L);
        verify(modelMapper, times(1)).map(filter, UserFilterDtoResponse.class);
    }

    @Test
    void deleteFilterByIdTest() {
        Filter filter = ModelUtils.getFilter();
        when(filterRepo.findById(1L)).thenReturn(Optional.of(filter));

        filterServiceImpl.deleteFilterById(filter.getId());
        verify(filterRepo, times(1)).findById(1L);
    }
}
