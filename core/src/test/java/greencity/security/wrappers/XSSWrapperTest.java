package greencity.security.wrappers;

import greencity.security.xss.XSSAllowedElements;
import greencity.security.xss.XSSSafelist;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class XSSWrapperTest {

    @Test
    void getInputStreamTest() throws IOException {
        String requestBody = "{\"text\":\"<script>alert('XSS')</script>Test\"}";
        String escapedBody = "{\"text\":\"Test\"}";
        HttpServletRequest mockRequest = mock(HttpServletRequest.class);

        when(mockRequest.getReader())
            .thenReturn(new BufferedReader(new InputStreamReader(new ByteArrayInputStream(requestBody.getBytes()))));
        when(mockRequest.getRequestURI()).thenReturn("/test");
        XSSAllowedElements allowedElements = XSSAllowedElements.getDefault();

        try (MockedStatic<XSSSafelist> mockedStatic = mockStatic(XSSSafelist.class)) {
            mockedStatic.when(() -> XSSSafelist.getAllowedElementsForEndpoint("/test")).thenReturn(allowedElements);
            XSSWrapper xssWrapper = new XSSWrapper(mockRequest);
            ServletInputStream inputStream = xssWrapper.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String result = reader.readLine();
            Assertions.assertEquals(escapedBody, result);
        }
    }
}
