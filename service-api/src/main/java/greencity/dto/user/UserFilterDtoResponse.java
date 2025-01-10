package greencity.dto.user;

import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class UserFilterDtoResponse {
    private Long id;
    private String name;
    private String searchCriteria;
    private String userStatus;
    private String userRole;
}
