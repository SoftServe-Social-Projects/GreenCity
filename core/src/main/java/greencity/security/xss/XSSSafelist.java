package greencity.security.xss;

import org.jsoup.safety.Safelist;
import org.springframework.util.AntPathMatcher;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * class stores allowed html tags, attributes, and values.
 *
 * @author Dmytro Dmytruk
 */
public class XSSSafelist {
    private static final Map<String, XSSAllowedElements> endpointRules = new HashMap<>();
    private static final XSSAllowedElements defaultAllowedElements = XSSAllowedElements.getDefault();
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();

    static {
        Safelist safelistForEvent = new Safelist()
            .addTags("p", "strong", "em", "u", "span")
            .addAttributes("span", "class");

        XSSAllowedElements allowedElementsForEvent = XSSAllowedElements.builder()
            .safelist(safelistForEvent)
            .fields(List.of("description"))
            .build();

        Safelist safelistForEcoNews = new Safelist()
            .addTags("pre", "p", "span", "a", "iframe", "img")
            .addAttributes("pre", "class")
            .addAttributes("p", "class")
            .addAttributes("span", "class", "style")
            .addAttributes("a", "href", "rel", "target")
            .addAttributes("iframe", "class", "src", "frameborder", "allowfullscreen")
            .addAttributes("img", "src", "alt");
        safelistForEcoNews.addEnforcedAttribute("iframe", "frameborder", "0")
            .addEnforcedAttribute("iframe", "allowfullscreen", "true")
            .addProtocols("a", "href", "http", "https");

        XSSAllowedElements allowedElementsForEcoNews = XSSAllowedElements.builder()
            .safelist(safelistForEcoNews)
            .fields(List.of("text", "content"))
            .build();

        Safelist safelistForHabit = new Safelist()
            .addTags("p", "strong", "em", "u", "span")
            .addAttributes("span", "class");

        XSSAllowedElements allowedElementsForHabit = XSSAllowedElements.builder()
            .safelist(safelistForHabit)
            .fields(List.of("description", "descriptionUa"))
            .build();

        endpointRules.put("/events/create", allowedElementsForEvent);
        endpointRules.put("/events/update", allowedElementsForEvent);
        endpointRules.put("/eco-news", allowedElementsForEcoNews);
        endpointRules.put("/eco-news/{id}", allowedElementsForEcoNews);
        endpointRules.put("/habit/custom", allowedElementsForHabit);
        endpointRules.put("/habit/update{id}", allowedElementsForHabit);
    }

    public static XSSAllowedElements getAllowedElementsForEndpoint(String endpoint) {
        for (String registeredEndpoint : endpointRules.keySet()) {
            if (pathMatcher.match(registeredEndpoint, endpoint)) {
                return endpointRules.get(registeredEndpoint);
            }
        }
        return defaultAllowedElements;
    }
}
