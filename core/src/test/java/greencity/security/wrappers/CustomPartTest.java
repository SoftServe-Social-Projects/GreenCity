package greencity.security.wrappers;

import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import static org.mockito.Mockito.when;

class CustomPartTest {
    private final String updatedContent = "updated content";
    private final byte[] updatedContentBytes = updatedContent.getBytes();

    @Test
    void getInputStreamTest() {
        Part originalPart = Mockito.mock(Part.class);
        CustomPart customPart = new CustomPart(originalPart, updatedContent);
        InputStream inputStream = customPart.getInputStream();
        Assertions.assertNotNull(inputStream);
    }

    @Test
    void getContentTypeTest() {
        Part originalPart = Mockito.mock(Part.class);
        when(originalPart.getContentType()).thenReturn("text/plain");

        CustomPart customPart = new CustomPart(originalPart, updatedContent);
        String contentType = customPart.getContentType();
        Assertions.assertEquals("text/plain", contentType);
    }

    @Test
    void getNameTest() {
        Part originalPart = Mockito.mock(Part.class);
        when(originalPart.getName()).thenReturn("testPart");

        CustomPart customPart = new CustomPart(originalPart, updatedContent);
        String name = customPart.getName();
        Assertions.assertEquals("testPart", name);
    }

    @Test
    void getSubmittedFileNameTest() {
        Part originalPart = Mockito.mock(Part.class);
        when(originalPart.getSubmittedFileName()).thenReturn("file.txt");

        CustomPart customPart = new CustomPart(originalPart, updatedContent);
        String fileName = customPart.getSubmittedFileName();
        Assertions.assertEquals("file.txt", fileName);
    }

    @Test
    void getSizeTest() {
        Part originalPart = Mockito.mock(Part.class);
        CustomPart customPart = new CustomPart(originalPart, updatedContent);

        long size = customPart.getSize();
        Assertions.assertEquals(updatedContentBytes.length, size);
    }

    @Test
    void writeTest() throws IOException {
        Part originalPart = Mockito.mock(Part.class);
        CustomPart customPart = new CustomPart(originalPart, updatedContent);

        String fileName = "testFile.txt";
        customPart.write(fileName);
        byte[] fileContent = Files.readAllBytes(Paths.get(fileName));
        Assertions.assertArrayEquals(updatedContentBytes, fileContent);
        Files.delete(Paths.get(fileName)); // clean up
    }

    @Test
    void getHeaderTest() {
        Part originalPart = Mockito.mock(Part.class);
        when(originalPart.getHeader("Content-Type")).thenReturn("text/plain");

        CustomPart customPart = new CustomPart(originalPart, updatedContent);
        String header = customPart.getHeader("Content-Type");
        Assertions.assertEquals("text/plain", header);
    }

    @Test
    void getHeadersTest() {
        Part originalPart = Mockito.mock(Part.class);
        when(originalPart.getHeaders("Content-Type")).thenReturn(List.of("text/plain"));

        CustomPart customPart = new CustomPart(originalPart, updatedContent);
        Collection<String> headers = customPart.getHeaders("Content-Type");
        Assertions.assertTrue(headers.contains("text/plain"));
    }

    @Test
    void getHeaderNamesTest() {
        Part originalPart = Mockito.mock(Part.class);
        when(originalPart.getHeaderNames()).thenReturn(List.of("Content-Type", "Content-Length"));
        CustomPart customPart = new CustomPart(originalPart, updatedContent);
        Collection<String> headerNames = customPart.getHeaderNames();
        Assertions.assertTrue(headerNames.contains("Content-Type"));
        Assertions.assertTrue(headerNames.contains("Content-Length"));
    }
}
