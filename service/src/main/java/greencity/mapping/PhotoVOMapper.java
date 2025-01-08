package greencity.mapping;

import greencity.dto.photo.PhotoVO;
import greencity.entity.Photo;
import org.modelmapper.AbstractConverter;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Component;

/**
 * Class that used by {@link ModelMapper} to map {@link Photo} into
 * {@link PhotoVO}.
 */
@Component
public class PhotoVOMapper extends AbstractConverter<Photo, PhotoVO> {
    /**
     * Method convert {@link Photo} to {@link PhotoVO}.
     *
     * @return {@link Photo}
     */
    @Override
    protected PhotoVO convert(Photo source) {
        return PhotoVO.builder()
            .id(source.getId())
            .name(source.getName())
            .commentId(source.getComment() == null ? null : source.getComment().getId())
            .placeId(source.getPlace().getId())
            .userId(source.getUser().getId())
            .build();
    }
}
