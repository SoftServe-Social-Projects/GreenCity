package greencity.filters;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import java.util.Objects;

@Builder
@Getter
@Setter
@ToString
public class SearchCriteria {
    private Object value;
    private String key;
    private String type;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SearchCriteria that = (SearchCriteria) o;

        if (!Objects.equals(value, that.value)) {
            return false;
        }
        if (!Objects.equals(key, that.key)) {
            return false;
        }
        return Objects.equals(type, that.type) && Objects.equals(value, that.value)
            && Objects.equals(key, that.key);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, key, type);
    }
}
