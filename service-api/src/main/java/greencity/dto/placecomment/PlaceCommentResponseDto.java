package greencity.dto.placecomment;

import greencity.dto.photo.PhotoReturnDto;
import greencity.dto.rate.EstimateAddDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlaceCommentResponseDto {
    private Long id;
    private String text;
    private LocalDateTime createdDate;
    private List<PhotoReturnDto> photos;
    private EstimateAddDto estimate;
}
