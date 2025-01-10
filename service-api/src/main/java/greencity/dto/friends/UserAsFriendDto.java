package greencity.dto.friends;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Setter
@ToString
@EqualsAndHashCode
public class UserAsFriendDto {
    private Long id;
    private String friendStatus;
    private Long requesterId;
    private Long chatId;

    public UserAsFriendDto(Long id, Long chatId) {
        this.id = id;
        this.chatId = chatId;
    }
}
