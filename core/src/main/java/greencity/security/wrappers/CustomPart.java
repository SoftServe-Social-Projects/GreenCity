package greencity.security.wrappers;

import jakarta.servlet.http.Part;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;

/**
 * Class that represents customized part of multipart/form-data content type
 * request.
 *
 * @author Dmytro Dmytruk.
 */
public class CustomPart implements Part {
    private final Part originalPart;
    private final byte[] content;

    public CustomPart(Part originalPart, String updatedContent) {
        this.originalPart = originalPart;
        this.content = updatedContent.getBytes();
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public String getContentType() {
        return originalPart.getContentType();
    }

    @Override
    public String getName() {
        return originalPart.getName();
    }

    @Override
    public String getSubmittedFileName() {
        return originalPart.getSubmittedFileName();
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public void write(String fileName) throws IOException {
        Files.write(Paths.get(fileName), content);
    }

    @Override
    public void delete() throws IOException {
        originalPart.delete();
    }

    @Override
    public String getHeader(String name) {
        return originalPart.getHeader(name);
    }

    @Override
    public Collection<String> getHeaders(String name) {
        return originalPart.getHeaders(name);
    }

    @Override
    public Collection<String> getHeaderNames() {
        return originalPart.getHeaderNames();
    }
}
