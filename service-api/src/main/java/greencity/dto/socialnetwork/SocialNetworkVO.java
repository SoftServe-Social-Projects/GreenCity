package greencity.dto.socialnetwork;

import greencity.dto.user.UserVO;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Data;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.Size;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
@Data
public class SocialNetworkVO {
    private Long id;

    @Size(min = 1, max = 500)
    String url;

    SocialNetworkImageVO socialNetworkImage;

    UserVO user;
}
