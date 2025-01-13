package greencity.dto.habit;

import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.Valid;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserToDoAndCustomToDoListsDto {
    @Valid
    List<@Valid UserToDoListItemResponseDto> userToDoListItemDto;
    @Valid
    List<@Valid CustomToDoListItemResponseDto> customToDoListItemDto;
}
