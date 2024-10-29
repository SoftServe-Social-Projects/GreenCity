package greencity.security.xss;

import lombok.experimental.UtilityClass;
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
@UtilityClass
public class XSSSafelist {
    private static final Map<String, XSSAllowedElements> endpointRules = new HashMap<>();
    private static final XSSAllowedElements defaultAllowedElements = XSSAllowedElements.getDefault();
    private static final AntPathMatcher pathMatcher = new AntPathMatcher();
    // Html tags
    private static final String P_TAG = "p";
    private static final String A_TAG = "a";
    private static final String U_TAG = "u";
    private static final String EM_TAG = "em";
    private static final String PRE_TAG = "pre";
    private static final String SPAN_TAG = "span";
    private static final String IMG_TAG = "img";
    private static final String STRONG_TAG = "strong";
    private static final String H1_TAG = "h1";
    private static final String H2_TAG = "h2";
    private static final String OL_TAG = "ol";
    private static final String LI_TAG = "li";
    private static final String BR_TAG = "br";
    private static final String S_TAG = "s";
    private static final String IFRAME_TAG = "iframe";
    // Html attributes
    private static final String CLASS_ATTR = "class";
    private static final String HREF_ATTR = "href";
    private static final String SRC_ATTR = "src";
    private static final String REL_ATTR = "rel";
    private static final String ALT_ATTR = "alt";
    private static final String STYLE_ATTR = "style";
    private static final String TARGET_ATTR = "target";
    private static final String FRAMEBORDER_ATTR = "frameborder";
    private static final String ALLOWFULLSCREEN_ATTR = "allowfullscreen";
    private static final String CONTENTEDITABLE_ATTR = "contenteditable";
    private static final String DATA_USER_ID_ATTR = "data-userid";
    private static final String DATA_NAME_ATTR = "data-name";

    static {
        Safelist safelistForEvent = new Safelist()
            .addTags(P_TAG, STRONG_TAG, EM_TAG, U_TAG, SPAN_TAG)
            .addAttributes(SPAN_TAG, CLASS_ATTR);

        XSSAllowedElements allowedElementsForEvent = XSSAllowedElements.builder()
            .safelist(safelistForEvent)
            .fields(List.of("description"))
            .build();

        Safelist safelistForEcoNews = new Safelist()
            .addTags(P_TAG, A_TAG, U_TAG, EM_TAG, PRE_TAG, SPAN_TAG, IMG_TAG, STRONG_TAG, H1_TAG, H2_TAG, OL_TAG,
                LI_TAG, BR_TAG, S_TAG, IFRAME_TAG)
            .addAttributes(H1_TAG, STYLE_ATTR)
            .addAttributes(H2_TAG, STYLE_ATTR)
            .addAttributes(STRONG_TAG, STYLE_ATTR)
            .addAttributes(EM_TAG, STYLE_ATTR)
            .addAttributes(U_TAG, STYLE_ATTR)
            .addAttributes(S_TAG, STYLE_ATTR)
            .addAttributes(BR_TAG, STYLE_ATTR)
            .addAttributes(OL_TAG, STYLE_ATTR)
            .addAttributes(LI_TAG, STYLE_ATTR)
            .addAttributes(SPAN_TAG, CLASS_ATTR, STYLE_ATTR, DATA_NAME_ATTR, CONTENTEDITABLE_ATTR)
            .addAttributes(P_TAG, CLASS_ATTR, STYLE_ATTR)
            .addAttributes(A_TAG, HREF_ATTR, REL_ATTR, TARGET_ATTR)
            .addAttributes(IMG_TAG, SRC_ATTR, ALT_ATTR)
            .addAttributes(IFRAME_TAG, SRC_ATTR, FRAMEBORDER_ATTR, ALLOWFULLSCREEN_ATTR, STYLE_ATTR, CLASS_ATTR)
            .addAttributes(":all", STYLE_ATTR, CLASS_ATTR, SRC_ATTR)
            .addEnforcedAttribute(IFRAME_TAG, FRAMEBORDER_ATTR, "0")
            .addEnforcedAttribute(IFRAME_TAG, ALLOWFULLSCREEN_ATTR, "true")
            .addProtocols(A_TAG, HREF_ATTR, "http", "https");

        XSSAllowedElements allowedElementsForEcoNews = XSSAllowedElements.builder()
            .safelist(safelistForEcoNews)
            .fields(List.of("text", "content"))
            .build();

        Safelist safelistForHabit = new Safelist()
            .addTags(P_TAG, STRONG_TAG, EM_TAG, U_TAG, SPAN_TAG)
            .addAttributes(SPAN_TAG, CLASS_ATTR);

        XSSAllowedElements allowedElementsForHabit = XSSAllowedElements.builder()
            .safelist(safelistForHabit)
            .fields(List.of("description", "descriptionUa"))
            .build();

        Safelist safelistForComments = new Safelist()
            .addTags(A_TAG)
            .addEnforcedAttribute(A_TAG, CONTENTEDITABLE_ATTR, "false")
            .addAttributes(A_TAG, STYLE_ATTR)
            .addAttributes(A_TAG, DATA_USER_ID_ATTR);

        XSSAllowedElements allowedElementsForComments = XSSAllowedElements.builder()
            .safelist(safelistForComments)
            .fields(List.of("text", "commentText"))
            .build();

        endpointRules.put("/events/create", allowedElementsForEvent);
        endpointRules.put("/events/update", allowedElementsForEvent);
        endpointRules.put("/eco-news", allowedElementsForEcoNews);
        endpointRules.put("/eco-news/{id}", allowedElementsForEcoNews);
        endpointRules.put("/habit/custom", allowedElementsForHabit);
        endpointRules.put("/habit/update{id}", allowedElementsForHabit);
        endpointRules.put("/events/{eventId}/comments", allowedElementsForComments);
        endpointRules.put("/events/{eventId}/comments/{commentId}", allowedElementsForComments);
        endpointRules.put("/eco-news/{ecoNewsId}/comments", allowedElementsForComments);
        endpointRules.put("/eco-news/{ecoNewsId}/comments/{commentId}", allowedElementsForComments);
        endpointRules.put("/habits/{habitId}/comments", allowedElementsForComments);
        endpointRules.put("/habits/comments", allowedElementsForComments);
        endpointRules.put("/place/{placeId}/comments", allowedElementsForComments);
    }

    public static XSSAllowedElements getAllowedElementsForEndpoint(String endpoint) {
        for (Map.Entry<String, XSSAllowedElements> entry : endpointRules.entrySet()) {
            if (pathMatcher.match(entry.getKey(), endpoint)) {
                return entry.getValue();
            }
        }
        return defaultAllowedElements;
    }
}
