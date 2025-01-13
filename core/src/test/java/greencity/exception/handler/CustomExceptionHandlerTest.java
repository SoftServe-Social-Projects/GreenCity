package greencity.exception.handler;

import greencity.exception.exceptions.*;
import jakarta.validation.ConstraintDeclarationException;
import jakarta.validation.ValidationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.powermock.api.mockito.PowerMockito.when;

@ExtendWith(MockitoExtension.class)
class CustomExceptionHandlerTest {
    @Mock
    WebRequest webRequest;
    @Mock
    ErrorAttributes errorAttributes;
    Map<String, Object> objectMap;
    @InjectMocks
    CustomExceptionHandler customExceptionHandler;
    @Mock
    HttpHeaders headers;
    @Mock
    MethodArgumentNotValidException notValidException;
    @Mock
    MethodArgumentTypeMismatchException mismatchException;
    @Mock
    HttpMessageNotReadableException ex;

    @BeforeEach
    void init() {
        MockitoAnnotations.openMocks(this);
        objectMap = new HashMap<>();
        objectMap.put("path", "/ownSecurity/restorePassword");
        objectMap.put("message", "test");
        objectMap.put("timestamp", "2021-02-06T17:27:50.569+0000");
        objectMap.put("trace", "Internal Server Error");
    }

    @Test
    void handleTooLargeMultipartFileRequest() {
        MultipartException multipartException = new MultipartException("test");

        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class),
            any(ErrorAttributeOptions.class))).thenReturn(objectMap);

        assertEquals(customExceptionHandler.handleTooLargeMultipartFileRequest(
            multipartException, webRequest),
            ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(exceptionResponse));
    }

    @Test
    void handleConstraintDeclarationException() {
        ConstraintDeclarationException constraintDeclarationException = new ConstraintDeclarationException("test");

        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class),
            any(ErrorAttributeOptions.class))).thenReturn(objectMap);

        assertEquals(customExceptionHandler.handleConstraintDeclarationException(
            constraintDeclarationException, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
    }

    @Test
    void handleValidationException() {
        ValidationException validationException = new ValidationException("test");

        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class),
            any(ErrorAttributeOptions.class))).thenReturn(objectMap);

        assertEquals(customExceptionHandler.handleValidationException(
            validationException, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
    }

    @Test
    void handleForbiddenException() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class),
            any(ErrorAttributeOptions.class))).thenReturn(objectMap);

        assertEquals(customExceptionHandler.handleForbiddenException(
            webRequest),
            ResponseEntity.status(HttpStatus.FORBIDDEN).body(exceptionResponse));
    }

    @Test
    void handleNotFoundException() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class),
            any(ErrorAttributeOptions.class))).thenReturn(objectMap);

        assertEquals(customExceptionHandler.handleNotFoundException(
            webRequest),
            ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse));
    }

    @Test
    void handleIllegalArgumentException() {
        IllegalArgumentException illegalArgumentException = new IllegalArgumentException("test");

        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class),
            any(ErrorAttributeOptions.class))).thenReturn(objectMap);

        assertEquals(customExceptionHandler.handleIllegalArgumentException(
            illegalArgumentException, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
    }

    @Test
    void handleStatusException() {
        InvalidStatusException statusException = new InvalidStatusException("test");

        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class),
            any(ErrorAttributeOptions.class))).thenReturn(objectMap);

        assertEquals(customExceptionHandler.handleStatusException(
            statusException, webRequest),
            ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionResponse));
    }

    @Test
    void handleOperationException() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class),
            any(ErrorAttributeOptions.class))).thenReturn(objectMap);

        assertEquals(customExceptionHandler.handleOperationException(
            webRequest),
            ResponseEntity.status(HttpStatus.CONFLICT).body(exceptionResponse));
    }

    @Test
    void handleUnsupportedOperationException() {
        UnsupportedOperationException unsupportedOperationException = new UnsupportedOperationException("test");

        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class),
            any(ErrorAttributeOptions.class))).thenReturn(objectMap);

        assertEquals(customExceptionHandler.handleUnsupportedOperationException(
            unsupportedOperationException, webRequest),
            ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED).body(exceptionResponse));
    }

    @Test
    void handleMethodArgumentNotValid() {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        FieldError fieldError = new FieldError("G", "field", "default");
        ValidationExceptionDto validationExceptionDto = new ValidationExceptionDto(fieldError);

        final BindingResult bindingResult = mock(BindingResult.class);

        when(notValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getFieldErrors()).thenReturn(Collections.singletonList(fieldError));
        assertEquals(
            customExceptionHandler.handleMethodArgumentNotValid(notValidException, headers, httpStatus, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.singletonList(validationExceptionDto)));
    }

    @Test
    void handleBadSocialNetworkLinkException() {
        InvalidURLException invalidURLException = new InvalidURLException("test");
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(), any())).thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleBadSocialNetworkLinkException(invalidURLException, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
    }

    @Test
    void testHandleBadSocialNetworkLinkException() {
        BadSocialNetworkLinksException badSocialNetworkLinksException = new BadSocialNetworkLinksException("test");
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(
            customExceptionHandler.handleBadSocialNetworkLinkException(badSocialNetworkLinksException, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
    }

    @Test
    void handleBadRequestException() {
        BadRequestException badRequestException = new BadRequestException("test");
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class),
            any(ErrorAttributeOptions.class))).thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleBadRequestException(
            badRequestException, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
    }

    @Test
    void handleHttpMessageNotReadable() {
        HttpStatus httpStatus = HttpStatus.BAD_REQUEST;
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleHttpMessageNotReadable(
            ex, headers, httpStatus, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
    }

    @Test
    void handleConversionFailedException() {
        ExceptionResponse exceptionResponse = new ExceptionResponse(objectMap);
        exceptionResponse.setMessage("Wrong null. Should be 'null'");
        when(errorAttributes.getErrorAttributes(any(WebRequest.class), any(ErrorAttributeOptions.class)))
            .thenReturn(objectMap);
        assertEquals(customExceptionHandler.handleConversionFailedException(mismatchException, webRequest),
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse));
    }
}