package greencity.dto.comment;

import greencity.dto.photo.PhotoReturnDto;
import greencity.dto.place.AdminPlaceDto;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommentAdminDto {
    private Long id;
    private String text;
    private LocalDateTime createdDate;
    private List<PhotoReturnDto> photos;
    private AdminPlaceDto place;
}
