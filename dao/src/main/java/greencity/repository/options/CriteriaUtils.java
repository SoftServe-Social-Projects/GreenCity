package greencity.repository.options;

import java.util.Optional;
import lombok.experimental.UtilityClass;

@UtilityClass
public class CriteriaUtils {
    /**
     * Returns a String criteria for search.
     *
     * @param criteria String for search.
     * @return String criteria not be {@literal null}.
     */
    static String replaceCriteria(String criteria) {
        criteria = Optional.ofNullable(criteria).orElse("");
        criteria = criteria.trim();
        criteria = criteria.replace("_", "\\_");
        criteria = criteria.replace("%", "\\%");
        criteria = criteria.replace("\\", "\\\\");
        criteria = criteria.replace("'", "\\'");
        criteria = "%" + criteria + "%";
        return criteria;
    }
}
