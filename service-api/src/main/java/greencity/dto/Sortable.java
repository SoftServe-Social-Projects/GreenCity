package greencity.dto;

import java.util.List;

/**
 * Represents a DTO that supports sorting.
 */
public interface Sortable {
    /**
     * Retrieves a list of allowed fields that can be used for sorting.
     *
     * @return a list of sortable fields as strings
     */
    List<String> getSortableFields();
}
