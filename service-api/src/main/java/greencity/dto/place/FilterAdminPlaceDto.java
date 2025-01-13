package greencity.dto.place;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@ToString
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@EqualsAndHashCode
@Builder
public class FilterAdminPlaceDto {
    private String id;
    private String name;
    private String status;
    private String author;
    private String address;

    public boolean isEmpty() {
        return (id == null || id.isEmpty())
            && (name == null || name.isEmpty())
            && (status == null || status.isEmpty())
            && (author == null || author.isEmpty())
            && (address == null || address.isEmpty());
    }
}
