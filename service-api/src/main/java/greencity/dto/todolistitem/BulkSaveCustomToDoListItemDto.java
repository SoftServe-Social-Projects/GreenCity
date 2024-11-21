package greencity.dto.todolistitem;

import java.util.List;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class BulkSaveCustomToDoListItemDto {
    @Valid
    List<@Valid CustomToDoListItemSaveRequestDto> customToDoListItemSaveRequestDtoList;
}
