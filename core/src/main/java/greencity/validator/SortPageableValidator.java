package greencity.validator;

import greencity.dto.Sortable;
import greencity.dto.friends.UserFriendDto;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
public class SortPageableValidator {
    private static final Map<Class<? extends Sortable>, List<String>> VALID_FIELDS_MAP = new HashMap<>();

    static {
        VALID_FIELDS_MAP.put(UserFriendDto.class, new UserFriendDto().getSortableFields());

    }

    public void validateSortParameters(Pageable pageable, Class<? extends Sortable> dtoClass) {
        List<String> validFields = VALID_FIELDS_MAP.get(dtoClass);

        if (validFields == null) {
            throw new IllegalArgumentException("Invalid DTO class");
        }

        pageable.getSort().forEach(order -> {
            String property = order.getProperty();
            if (!validFields.contains(property)) {
                throw new IllegalArgumentException("Invalid sort property: " + property);
            }
        });
    }
}
