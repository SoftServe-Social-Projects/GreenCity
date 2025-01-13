package greencity.security.wrappers;

import greencity.constant.ErrorMessage;
import greencity.exception.exceptions.MultipartXSSProcessingException;
import greencity.security.xss.XSSAllowedElements;
import greencity.security.xss.XSSSafelist;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.Part;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.Collections;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MultipartXSSWrapperTest {

    @Test
    void testGetPart() throws IOException, ServletException {
        String requestBody = "{\"key\":\"<script>alert('XSS')</script>Test\"}";
        String escapedBody = "{\"key\":\"Test\"}";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Part mockPart = mock(Part.class);

        when(mockPart.getInputStream()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
        when(mockRequest.getRequestURI()).thenReturn("/test");
        when(mockRequest.getPart("partName")).thenReturn(mockPart);
        XSSAllowedElements allowedElements = XSSAllowedElements.getDefault();

        try (MockedStatic<XSSSafelist> mockedStatic = mockStatic(XSSSafelist.class)) {
            mockedStatic.when(() -> XSSSafelist.getAllowedElementsForEndpoint("/test")).thenReturn(allowedElements);
            MultipartXSSWrapper xssWrapper = new MultipartXSSWrapper(mockRequest);
            Part part = xssWrapper.getPart("partName");
            BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream()));
            String result = reader.readLine();
            Assertions.assertEquals(escapedBody, result);
        }
    }

    @Test
    void testGetParts() throws IOException, ServletException {
        String requestBody = "{\"key\":\"<b>test</b>\"}";
        String escapedBody = "{\"key\":\"test\"}";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Part mockPart = mock(Part.class);

        when(mockPart.getInputStream()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
        when(mockRequest.getRequestURI()).thenReturn("/test");
        when(mockRequest.getParts()).thenReturn(Collections.singletonList(mockPart));
        XSSAllowedElements allowedElements = XSSAllowedElements.getDefault();

        try (MockedStatic<XSSSafelist> mockedStatic = mockStatic(XSSSafelist.class)) {
            mockedStatic.when(() -> XSSSafelist.getAllowedElementsForEndpoint("/test")).thenReturn(allowedElements);
            MultipartXSSWrapper xssWrapper = new MultipartXSSWrapper(mockRequest);
            Collection<Part> parts = xssWrapper.getParts();
            Part part = parts.iterator().next();
            BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream()));
            String result = reader.readLine();
            Assertions.assertEquals(escapedBody, result);
        }
    }

    @Test
    void testIOExceptionHandling() throws IOException, ServletException {
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Part mockPart = mock(Part.class);

        when(mockPart.getInputStream()).thenThrow(new IOException("Test IOException"));
        when(mockRequest.getRequestURI()).thenReturn("/test");
        when(mockRequest.getPart("partName")).thenReturn(mockPart);

        MultipartXSSWrapper xssWrapper = new MultipartXSSWrapper(mockRequest);

        MultipartXSSProcessingException exception = Assertions.assertThrows(
            MultipartXSSProcessingException.class,
            () -> xssWrapper.getPart("partName"));
        Assertions.assertEquals(ErrorMessage.XSS_MULTIPART_PROCESSING_ERROR, exception.getMessage());
    }

    @Test
    void testEmptyPart() throws IOException, ServletException {
        String requestBody = "";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);
        Part mockPart = mock(Part.class);

        when(mockPart.getInputStream()).thenReturn(new ByteArrayInputStream(requestBody.getBytes()));
        when(mockRequest.getRequestURI()).thenReturn("/test");
        when(mockRequest.getPart("partName")).thenReturn(mockPart);

        MultipartXSSWrapper xssWrapper = new MultipartXSSWrapper(mockRequest);
        Part part = xssWrapper.getPart("partName");

        BufferedReader reader = new BufferedReader(new InputStreamReader(part.getInputStream()));
        String result = reader.readLine();
        Assertions.assertNull(result);
    }
}