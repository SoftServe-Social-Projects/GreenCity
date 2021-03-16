package greencity.dto.shoppinglistitem;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotEmpty;

import greencity.enums.ShoppingListItemStatus;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class CustomShoppingListItemResponseDto {
    @NonNull
    @Min(1)
    private Long id;
    @NotEmpty
    private String text;
    @NotEmpty
    private ShoppingListItemStatus status;
}
