package greencity.dto.user;

import greencity.enums.ShoppingListItemStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class UserShoppingListItemResponseDto {
    @Min(1L)
    private Long id;
    @NotEmpty
    private String text;
    @NotNull
    private ShoppingListItemStatus status;
}
