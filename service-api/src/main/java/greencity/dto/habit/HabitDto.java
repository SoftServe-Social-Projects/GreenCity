package greencity.dto.habit;

import greencity.constant.ServiceValidationConstants;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.ShoppingListItemDto;
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
public class HabitDto {
    private Integer defaultDuration;
    private Long amountAcquiredUsers;
    private HabitTranslationDto habitTranslation;
    private Long id;
    private String image;
    @Min(value = 1, message = ServiceValidationConstants.HABIT_COMPLEXITY)
    @Max(value = 3, message = ServiceValidationConstants.HABIT_COMPLEXITY)
    private Integer complexity;
    private List<String> tags;
    private List<ShoppingListItemDto> shoppingListItems;
    private List<CustomShoppingListItemResponseDto> customShoppingListItems;
    private Boolean isCustomHabit;
    private Long usersIdWhoCreatedCustomHabit;
    private HabitAssignStatus habitAssignStatus;
    private Boolean isAssigned;
}
