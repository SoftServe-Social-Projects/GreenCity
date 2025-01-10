package greencity.security.wrappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.constant.ErrorMessage;
import greencity.exception.exceptions.MultipartXSSProcessingException;
import greencity.security.xss.XSSAllowedElements;
import greencity.security.xss.XSSEscaper;
import greencity.security.xss.XSSSafelist;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.Part;
import java.io.IOException;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * class provides wrapping of multipart/form-data content type with escaping to
 * prevent xss attacks.
 *
 * @author Dmytro Dmytruk.
 */
public class MultipartXSSWrapper extends HttpServletRequestWrapper {
    private final XSSAllowedElements allowedElements;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MultipartXSSWrapper(HttpServletRequest servletRequest) {
        super(servletRequest);
        allowedElements = XSSSafelist.getAllowedElementsForEndpoint(servletRequest.getRequestURI());
    }

    @Override
    public Part getPart(String name) throws IOException, ServletException {
        Part originalPart = super.getPart(name);
        return modifyPart(originalPart);
    }

    @Override
    public Collection<Part> getParts() throws IOException, ServletException {
        return super.getParts().stream()
            .map(this::modifyPart)
            .collect(Collectors.toList());
    }

    /**
     * Performs modification of part.
     *
     * @param originalPart {@link Part} - original part to escape.
     *
     * @return {@link Part} escaped part of the file.
     */
    private Part modifyPart(Part originalPart) {
        try {
            String body = new String(originalPart.getInputStream().readAllBytes());
            if (isJson(body)) {
                JsonNode rootNode = objectMapper.readTree(body);
                JsonNode cleanedNode = XSSEscaper.cleanJson(rootNode, allowedElements);
                String escapedBody = objectMapper.writeValueAsString(cleanedNode);
                return new CustomPart(originalPart, escapedBody);
            } else {
                return originalPart;
            }
        } catch (IOException e) {
            throw new MultipartXSSProcessingException(ErrorMessage.XSS_MULTIPART_PROCESSING_ERROR);
        }
    }

    /**
     * Determines if the content is JSON.
     *
     * @param content The content to check.
     *
     * @return true if the content is JSON, false otherwise.
     */
    private boolean isJson(String content) {
        try {
            JsonNode jsonNode = objectMapper.readTree(content);
            return jsonNode.isObject() || jsonNode.isArray();
        } catch (IOException e) {
            return false;
        }
    }
}