package greencity.dto.event;

import greencity.dto.user.UserVO;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class EventVO {
    private Long id;
    private String title;
    private String titleImage;
    private UserVO organizer;
    private String description;
}
