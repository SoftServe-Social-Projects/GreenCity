package greencity.dto.shoppinglistitem;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import greencity.enums.ShoppingListItemStatus;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.Builder;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
@EqualsAndHashCode
public class CustomShoppingListItemResponseDto {
    @Min(1)
    private Long id;
    @NotEmpty
    private String text;
    @NotNull
    private ShoppingListItemStatus status;
}
