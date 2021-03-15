package greencity.dto.user;

import greencity.dto.shoppinglistitem.ShoppingListItemRequestDto;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
public class UserShoppingListItemDto {
    @Valid
    @NotNull
    private ShoppingListItemRequestDto shoppingListItemRequestDto;
}
