package greencity.validator;

import greencity.constant.ErrorMessage;
import greencity.dto.Sortable;
import greencity.dto.comment.CommentDto;
import greencity.dto.econews.EcoNewsGenericDto;
import greencity.dto.factoftheday.FactOfTheDayDTO;
import greencity.dto.friends.UserFriendDto;
import greencity.dto.habit.HabitDto;
import greencity.dto.habit.HabitManagementDto;
import greencity.dto.place.AdminPlaceDto;
import greencity.dto.placecomment.PlaceCommentAdminDto;
import greencity.dto.ratingstatistics.RatingPointsDto;
import greencity.dto.ratingstatistics.RatingStatisticsDtoForTables;
import greencity.exception.exceptions.UnsupportedSortException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

@Component
public class SortPageableValidator {
    private static final Map<Class<? extends Sortable>, List<String>> VALID_FIELDS_MAP = new HashMap<>();

    static {
        VALID_FIELDS_MAP.put(AdminPlaceDto.class, new AdminPlaceDto().getSortableFields());
        VALID_FIELDS_MAP.put(CommentDto.class, new CommentDto().getSortableFields());
        VALID_FIELDS_MAP.put(EcoNewsGenericDto.class, new EcoNewsGenericDto().getSortableFields());
        VALID_FIELDS_MAP.put(FactOfTheDayDTO.class, new FactOfTheDayDTO().getSortableFields());
        VALID_FIELDS_MAP.put(HabitDto.class, new HabitDto().getSortableFields());
        VALID_FIELDS_MAP.put(HabitManagementDto.class, new HabitManagementDto().getSortableFields());
        VALID_FIELDS_MAP.put(PlaceCommentAdminDto.class, new PlaceCommentAdminDto().getSortableFields());
        VALID_FIELDS_MAP.put(RatingPointsDto.class, new RatingPointsDto().getSortableFields());
        VALID_FIELDS_MAP.put(RatingStatisticsDtoForTables.class,
            new RatingStatisticsDtoForTables().getSortableFields());
        VALID_FIELDS_MAP.put(UserFriendDto.class, new UserFriendDto().getSortableFields());
    }

    public void validateSortParameters(Sort sort, Class<? extends Sortable> dtoClass) {
        List<String> validFields = VALID_FIELDS_MAP.get(dtoClass);

        if (validFields == null) {
            throw new IllegalArgumentException(ErrorMessage.INVALID_DTO_CLASS);
        }

        sort.forEach(order -> {
            String property = order.getProperty();
            if (!validFields.contains(property)) {
                throw new UnsupportedSortException(ErrorMessage.INVALID_SORTING_VALUE);
            }
        });
    }
}
