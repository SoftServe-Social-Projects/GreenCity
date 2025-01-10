package greencity.security.xss;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.experimental.UtilityClass;
import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;
import java.util.Iterator;

/**
 * Class that escapes request with specific whitelist for each endpoint.
 *
 * @author Dmytro Dmytruk.
 */
@UtilityClass
public class XSSEscaper {
    /**
     * Escaping with allowed tags, attributes and attribute values using Jsoup.
     *
     * @param input    {@link String} text to escape
     * @param safelist allowed attribute values, key - tag, val - attributes with
     *                 allowed values
     *
     * @return escaped text
     */
    public static String clean(String input, Safelist safelist) {
        if (safelist.equals(Safelist.none())) {
            return Jsoup.clean(input, Safelist.none());
        }
        return containsHtmlTags(input) ? Jsoup.clean(input, safelist) : input;
    }

    /**
     * Checks if text contains html.
     *
     * @param text input text
     *
     * @return true if html present, false if not
     */
    public static boolean containsHtmlTags(String text) {
        return text.contains("<") && text.contains(">");
    }

    public static JsonNode cleanJson(JsonNode node, XSSAllowedElements allowedElements) {
        if (node.isObject()) {
            return cleanObjectNode((ObjectNode) node, allowedElements);
        } else if (node.isArray()) {
            return cleanArrayNode(node, allowedElements);
        }
        return node;
    }

    /**
     * Used to clean node of object.
     *
     * @param objectNode      {@link ObjectNode} represents node of json.
     *
     * @param allowedElements {@link XSSAllowedElements} list of allowed elements.
     *
     * @return clear Json node
     */
    private static JsonNode cleanObjectNode(ObjectNode objectNode, XSSAllowedElements allowedElements) {
        Iterator<String> fieldNames = objectNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode fieldValue = objectNode.get(fieldName);
            if (allowedElements.getFields().contains(fieldName.toLowerCase())) {
                if (fieldValue.isTextual()) {
                    objectNode.set(fieldName,
                        new TextNode(XSSEscaper.clean(fieldValue.asText(), allowedElements.getSafelist())));
                }
            } else {
                if (fieldValue.isObject() || fieldValue.isArray()) {
                    objectNode.set(fieldName, cleanJson(fieldValue, XSSAllowedElements.getDefault()));
                } else if (fieldValue.isTextual()) {
                    objectNode.set(fieldName, new TextNode(XSSEscaper.clean(fieldValue.asText(), Safelist.none())));
                }
            }
        }
        return objectNode;
    }

    /**
     * Perform cleaning of json array.
     *
     * @param node            {@link JsonNode} represents node of json.
     *
     * @param allowedElements {@link XSSAllowedElements} list of allowed elements.
     *
     * @return clean node
     */
    private static JsonNode cleanArrayNode(JsonNode node, XSSAllowedElements allowedElements) {
        for (JsonNode arrayElement : node) {
            cleanJson(arrayElement, allowedElements);
        }
        return node;
    }
}
