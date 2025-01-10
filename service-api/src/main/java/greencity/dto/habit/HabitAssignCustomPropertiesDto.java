package greencity.dto.habit;

import greencity.dto.todolistitem.CustomToDoListItemSaveRequestDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class HabitAssignCustomPropertiesDto {
    private HabitAssignPropertiesDto habitAssignPropertiesDto;
    private List<Long> friendsIdsList;
    private List<CustomToDoListItemSaveRequestDto> customToDoListItemList;
}
