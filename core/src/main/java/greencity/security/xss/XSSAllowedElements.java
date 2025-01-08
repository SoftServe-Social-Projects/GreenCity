package greencity.security.xss;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jsoup.safety.Safelist;
import java.util.List;

/**
 * class user for representing allowed html elements in xss escaping.
 *
 * @author Dmytro Dmytruk
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class XSSAllowedElements {
    private Safelist safelist;
    private List<String> fields;

    public static XSSAllowedElements getDefault() {
        return new XSSAllowedElements(Safelist.none(), List.of());
    }
}
