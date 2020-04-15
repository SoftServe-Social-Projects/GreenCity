package greencity.mapping;

import greencity.dto.location.LocationDto;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.entity.FavoritePlace;
import lombok.AllArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;


/**
 * The class uses other {@code Autowired} mappers to convert {@link FavoritePlace} entity objects to {@link
 * PlaceByBoundsDto} dto objects.
 *
 * @author Zakhar Skaletskyi
 */
@AllArgsConstructor
@Component
public class FavoritePlaceWithLocationMapper implements MapperToDto<FavoritePlace, PlaceByBoundsDto> {
    private ModelMapper modelMapper;

    @Override
    public PlaceByBoundsDto convertToDto(FavoritePlace entity) {
        PlaceByBoundsDto placeByBoundsDto = new PlaceByBoundsDto();
        placeByBoundsDto.setId(entity.getPlace().getId());
        placeByBoundsDto.setName(entity.getName());
        placeByBoundsDto.setLocation(modelMapper.map(entity.getPlace().getLocation(), LocationDto.class));
        return placeByBoundsDto;
    }
}
