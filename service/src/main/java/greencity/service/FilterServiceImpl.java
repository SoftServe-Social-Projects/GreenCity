package greencity.service;

import greencity.constant.ErrorMessage;
import greencity.dto.user.UserFilterDtoRequest;
import greencity.dto.user.UserFilterDtoResponse;
import greencity.entity.Filter;
import greencity.entity.User;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.FilterRepo;
import greencity.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FilterServiceImpl implements FilterService {
    private final FilterRepo filterRepo;
    private final UserRepo userRepo;
    private final ModelMapper modelMapper;

    @Override
    public UserFilterDtoResponse save(Long userId, UserFilterDtoRequest dto) {
        List<Filter> filters = filterRepo.getAllFilters(userId);
        if (filters.size() == 3) {
            return modelMapper.map(filters.getFirst(), UserFilterDtoResponse.class);
        }
        Filter filter = modelMapper.map(dto, Filter.class);
        User user = userRepo.findById(userId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.USER_NOT_FOUND_BY_ID + userId));
        filter.setUser(user);
        return modelMapper.map(filterRepo.save(filter), UserFilterDtoResponse.class);
    }

    @Override
    public List<UserFilterDtoResponse> getAllFilters(Long userId) {
        List<Filter> filters = filterRepo.getAllFilters(userId);
        return buildUserFilterDtoResponse(filters);
    }

    private List<UserFilterDtoResponse> buildUserFilterDtoResponse(List<Filter> filters) {
        return filters
            .stream()
            .map(filter -> modelMapper.map(filter, UserFilterDtoResponse.class))
            .collect(Collectors.toList());
    }

    @Override
    public UserFilterDtoResponse getFilterById(Long filterId) {
        Filter filter = filterRepo.findById(filterId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.FILTER_NOT_FOUND_BY_ID + filterId));

        return modelMapper.map(filter, UserFilterDtoResponse.class);
    }

    @Override
    public void deleteFilterById(Long filterId) {
        filterRepo.delete(filterRepo.findById(filterId)
            .orElseThrow(() -> new NotFoundException(ErrorMessage.FILTER_NOT_FOUND_BY_ID + filterId)));
    }
}
