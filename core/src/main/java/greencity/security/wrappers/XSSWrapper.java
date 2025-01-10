package greencity.security.wrappers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.security.xss.XSSAllowedElements;
import greencity.security.xss.XSSEscaper;
import greencity.security.xss.XSSSafelist;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.http.HttpHeaders;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Enumeration;
import java.util.stream.Collectors;

/**
 * Class provides wrapping of application/json content type with escaping to
 * prevent xss attacks.
 *
 * @author Dmytro Dmytruk
 */
public class XSSWrapper extends HttpServletRequestWrapper {
    private final String body;
    private final String escapedBody;

    public XSSWrapper(HttpServletRequest servletRequest) throws IOException {
        super(servletRequest);
        XSSAllowedElements allowedElements = XSSSafelist.getAllowedElementsForEndpoint(servletRequest.getRequestURI());
        body = servletRequest.getReader().lines().collect(Collectors.joining(System.lineSeparator()));
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode rootNode = objectMapper.readTree(body);
        JsonNode cleanedNode = XSSEscaper.cleanJson(rootNode, allowedElements);
        escapedBody = objectMapper.writeValueAsString(cleanedNode);
    }

    /**
     * Modify input stream of servlet with escaping of request body.
     *
     * @return {@link ServletInputStream} - modified input stream
     */
    @Override
    public ServletInputStream getInputStream() {
        final ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(escapedBody.getBytes());
        return new ServletInputStream() {
            @Override
            public int read() {
                return byteArrayInputStream.read();
            }

            @Override
            public boolean isFinished() {
                return false;
            }

            @Override
            public boolean isReady() {
                return false;
            }

            @Override
            public void setReadListener(ReadListener listener) {
                // Do nothing
            }
        };
    }

    @Override
    public BufferedReader getReader() {
        return new BufferedReader(new InputStreamReader(this.getInputStream()));
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        if (HttpHeaders.CONTENT_LENGTH.equals(name)) {
            return Collections.enumeration(Collections.singletonList(String.valueOf(body.length())));
        }
        return super.getHeaders(name);
    }
}