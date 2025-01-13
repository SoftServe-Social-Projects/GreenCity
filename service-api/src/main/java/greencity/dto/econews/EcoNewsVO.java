package greencity.dto.econews;

import greencity.dto.tag.TagVO;
import greencity.dto.user.UserVO;
import java.util.List;
import java.util.Set;
import java.util.HashSet;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;
import java.time.ZonedDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode(exclude = {"tags"})
public class EcoNewsVO {
    private Long id;

    private ZonedDateTime creationDate;

    private String imagePath;

    private String source;

    private UserVO author;

    private String title;

    private String text;

    @Builder.Default
    private Set<UserVO> usersLikedNews = new HashSet<>();

    private List<TagVO> tags;

    @Builder.Default
    private Set<UserVO> usersDislikedNews = new HashSet<>();
}
