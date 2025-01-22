package greencity.dto.habit;

import greencity.constant.ServiceValidationConstants;
import greencity.dto.Sortable;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.todolistitem.ToDoListItemDto;
import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import greencity.enums.HabitAssignStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@EqualsAndHashCode
public class HabitDto implements Sortable {
    private Integer defaultDuration;
    private Long amountAcquiredUsers;
    private HabitTranslationDto habitTranslation;
    private Long id;
    private String image;
    @Min(value = 1, message = ServiceValidationConstants.HABIT_COMPLEXITY)
    @Max(value = 3, message = ServiceValidationConstants.HABIT_COMPLEXITY)
    private Integer complexity;
    private List<String> tags;
    private List<ToDoListItemDto> toDoListItems;
    private List<CustomToDoListItemResponseDto> customToDoListItems;
    private Boolean isCustomHabit;
    private Long usersIdWhoCreatedCustomHabit;
    private HabitAssignStatus habitAssignStatus;
    private Boolean isAssigned;
    private Boolean isFavorite;
    private int likes;
    private int dislikes;

    @Override
    public List<String> getSortableFields() {
        return List.of();
    }
}
