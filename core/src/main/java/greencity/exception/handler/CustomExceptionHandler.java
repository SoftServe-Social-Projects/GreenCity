package greencity.exception.handler;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.constant.ErrorMessage;
import greencity.constant.ValidationConstants;
import greencity.exception.exceptions.BadCategoryRequestException;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.BadSocialNetworkLinksException;
import greencity.exception.exceptions.EventDtoValidationException;
import greencity.exception.exceptions.InvalidStatusException;
import greencity.exception.exceptions.InvalidURLException;
import greencity.exception.exceptions.MultipartXSSProcessingException;
import greencity.exception.exceptions.NotCurrentUserException;
import greencity.exception.exceptions.NotDeletedException;
import greencity.exception.exceptions.NotFoundException;
import greencity.exception.exceptions.NotSavedException;
import greencity.exception.exceptions.NotUpdatedException;
import greencity.exception.exceptions.ToDoListItemNotFoundException;
import greencity.exception.exceptions.TagNotFoundException;
import greencity.exception.exceptions.UnsupportedSortException;
import greencity.exception.exceptions.UserAlreadyHasEnrolledHabitAssign;
import greencity.exception.exceptions.UserAlreadyHasHabitAssignedException;
import greencity.exception.exceptions.UserHasNoFriendWithIdException;
import greencity.exception.exceptions.UserHasNoPermissionToAccessException;
import greencity.exception.exceptions.UserHasNoToDoListItemsException;
import greencity.exception.exceptions.UserHasReachedOutOfEnrollRange;
import greencity.exception.exceptions.UserToDoListItemStatusNotUpdatedException;
import greencity.exception.exceptions.WrongIdException;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.error.ErrorAttributeOptions;
import org.springframework.boot.web.servlet.error.ErrorAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Custom exception handler.
 */
@AllArgsConstructor
@RestControllerAdvice
@Slf4j
public class CustomExceptionHandler extends ResponseEntityExceptionHandler {
    private ErrorAttributes errorAttributes;
    private final ObjectMapper objectMapper;

    /**
     * ExceptionHandler for intercepting errors from GreenCityUser.
     *
     * @param ex      exception thrown by RestTemplate
     * @param request request with details
     * @return response entity similar to one that came from GreenCityUser
     */
    @ExceptionHandler(HttpClientErrorException.class)
    public final ResponseEntity<Object> handleHttpClientErrorException(
        HttpClientErrorException ex, WebRequest request) throws JsonProcessingException {
        Map<String, String> httpClientResponseBody = jsonHttpClientErrorExceptionToMap(ex);
        String message = httpClientResponseBody.get("message");
        log.warn("{} {}", ex.getStatusCode(), message);
        HttpClientErrorExceptionResponse responseBody =
            new HttpClientErrorExceptionResponse(getErrorAttributes(request), message);
        return ResponseEntity.status(ex.getStatusCode()).body(responseBody);
    }

    private Map<String, String> jsonHttpClientErrorExceptionToMap(
        HttpClientErrorException ex) throws JsonProcessingException {
        TypeReference<Map<String, String>> responseType = new TypeReference<>() {
        };
        Map<String, String> httpClientResponseBody;
        httpClientResponseBody = objectMapper.readValue(ex.getResponseBodyAsString(), responseType);

        return httpClientResponseBody;
    }

    /**
     * Method intercept exception {@link MultipartException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception.
     * @return ResponseEntity which contains http status and body with message of
     *         exception.
     * @author Danylo Hlynskyi
     */
    @ExceptionHandler(MultipartException.class)
    public final ResponseEntity<Object> handleTooLargeMultipartFileRequest(MultipartException ex,
        WebRequest request) {
        log.warn(ex.getMessage());
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link UserAlreadyHasHabitAssignedException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(UserAlreadyHasHabitAssignedException.class)
    public final ResponseEntity<Object> handleUserAlreadyHasHabitAssignedException(
        UserAlreadyHasHabitAssignedException ex, WebRequest request) {
        log.warn(ex.getMessage());
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link UserAlreadyHasHabitAssignedException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(UserHasReachedOutOfEnrollRange.class)
    public final ResponseEntity<Object> handleUserHasReachedMaxDaysInPastToEnrollHabit(
        UserHasReachedOutOfEnrollRange ex, WebRequest request) {
        log.warn(ex.getMessage());
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link UserAlreadyHasHabitAssignedException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity which contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(UserAlreadyHasEnrolledHabitAssign.class)
    public final ResponseEntity<Object> handleUserAlreadyHasEnrolledHabitAssign(
        UserAlreadyHasEnrolledHabitAssign ex, WebRequest request) {
        log.warn(ex.getMessage());
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link ConstraintViolationException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     * @author Yurii Savchenko
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public final ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex,
        WebRequest request) {
        log.warn(ex.getMessage());
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        String detailedMessage = ex.getConstraintViolations().stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(" "));
        exceptionResponse.setMessage(detailedMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link IllegalArgumentException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     * @author Volodymyr Mladonov
     */
    @ExceptionHandler({IllegalArgumentException.class})
    public ResponseEntity<Object> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
        log.warn(ex.getMessage());
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        if (ex.getMessage().startsWith(ValidationConstants.LOCALE_PART_ERROR_MESSAGE)) {
            exceptionResponse.setMessage(ValidationConstants.LANGUAGE_VALIDATION_EXCEPTION_MESSAGE);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link BadRequestException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     * @author Yurii Savchenko
     */
    @ExceptionHandler(BadRequestException.class)
    public final ResponseEntity<Object> handleBadRequestException(BadRequestException ex, WebRequest request) {
        log.warn(ex.getMessage());
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link NotFoundException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     * @author Marian Milian
     */
    @ExceptionHandler(NotFoundException.class)
    public final ResponseEntity<Object> handleNotFoundException(NotFoundException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link MethodArgumentTypeMismatchException}.
     *
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     * @author Roman Zahorui
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public final ResponseEntity<Object> handleConversionFailedException(
        @NonNull MethodArgumentTypeMismatchException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        String propName = ex.getName();
        String className = null;
        Class<?> requiredType = ex.getRequiredType();
        if (requiredType != null) {
            className = requiredType.getSimpleName();
        }
        String message = "Wrong %s. Should be '%s'".formatted(propName, className);
        exceptionResponse.setMessage(message);
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link BadSocialNetworkLinksException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(BadSocialNetworkLinksException.class)
    public final ResponseEntity<Object> handleBadSocialNetworkLinkException(BadSocialNetworkLinksException ex,
        WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link InvalidURLException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(InvalidURLException.class)
    public final ResponseEntity<Object> handleBadSocialNetworkLinkException(InvalidURLException ex,
        WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link DateTimeParseException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(DateTimeParseException.class)
    public final ResponseEntity<Object> handleDateTimeParseException(DateTimeParseException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        exceptionResponse.setMessage(ErrorMessage.WRONG_DATE_TIME_FORMAT);
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link BadCategoryRequestException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(BadCategoryRequestException.class)
    public final ResponseEntity<Object> handleBadCategoryRequestException(BadCategoryRequestException ex,
        WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link NotCurrentUserException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(NotCurrentUserException.class)
    public final ResponseEntity<Object> handleUserToDoListItemWhereNotSavedException(NotCurrentUserException ex,
        WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link ToDoListItemNotFoundException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(ToDoListItemNotFoundException.class)
    public final ResponseEntity<Object> handleUserToDoListItemWhereNotSavedException(
        ToDoListItemNotFoundException ex,
        WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link UserHasNoToDoListItemsException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(UserHasNoToDoListItemsException.class)
    public final ResponseEntity<Object> handleUserToDoListItemWhereNotSavedException(
        UserHasNoToDoListItemsException ex,
        WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link UserToDoListItemStatusNotUpdatedException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(UserToDoListItemStatusNotUpdatedException.class)
    public final ResponseEntity<Object> handleUserToDoListItemWhereNotSavedException(
        UserToDoListItemStatusNotUpdatedException ex,
        WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link TagNotFoundException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(TagNotFoundException.class)
    public final ResponseEntity<Object> handleTagNotFoundException(TagNotFoundException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Method intercept exception {@link UserHasNoPermissionToAccessException}.
     *
     * @param ex      Exception witch should be intercepted.
     * @param request contain detail about occur exception
     * @return ResponseEntity witch contain http status and body with message of
     *         exception.
     */
    @ExceptionHandler(UserHasNoPermissionToAccessException.class)
    public final ResponseEntity<Object> handleUserHasNoPermissionToAccessException(
        UserHasNoPermissionToAccessException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(exceptionResponse);
    }

    /**
     * Customize the response for NotSavedException.
     *
     * @param ex      the exception
     * @param request the current request
     * @return a {@code ResponseEntity} message
     */
    @ExceptionHandler(NotSavedException.class)
    public final ResponseEntity<Object> handleNotSavedException(
        NotSavedException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Customize the response for NotUpdatedException.
     *
     * @param ex      the exception
     * @param request the current request
     * @return a {@code ResponseEntity} message
     */
    @ExceptionHandler(NotUpdatedException.class)
    public final ResponseEntity<Object> handleNotUpdatedException(
        NotUpdatedException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Customize the response for NotDeletedException.
     *
     * @param ex      the exception
     * @param request the current request
     * @return a {@code ResponseEntity} message
     */
    @ExceptionHandler(NotDeletedException.class)
    public final ResponseEntity<Object> handleNotDeletedException(
        NotDeletedException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @Override
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Customize the response for InvalidStatusException.
     *
     * @param ex      the exception
     * @param request the current request
     * @return a {@code ResponseEntity} message
     */
    @ExceptionHandler(InvalidStatusException.class)
    public final ResponseEntity<Object> handleInvalidStatusException(InvalidStatusException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Customize the response for UnsupportedSortException.
     *
     * @param ex      the exception
     * @param request the current request
     * @return a {@code ResponseEntity} message
     */
    @ExceptionHandler(UnsupportedSortException.class)
    public final ResponseEntity<Object> handleUnsupportedSortException(
        UnsupportedSortException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Customize the response for EventDtoValidationException.
     *
     * @param ex      the exception
     * @param request the current request
     * @return a {@code ResponseEntity} message
     */
    @ExceptionHandler(EventDtoValidationException.class)
    public final ResponseEntity<Object> handleEventDtoValidationException(
        EventDtoValidationException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Customize the response for WrongIdException.
     *
     * @param ex      the exception
     * @param request the current request
     * @return a {@code ResponseEntity} message
     */
    @ExceptionHandler(WrongIdException.class)
    public final ResponseEntity<Object> handleWrongIdException(
        WrongIdException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
        HttpHeaders headers, HttpStatusCode status, WebRequest request) {
        List<ValidationExceptionDto> collect =
            ex.getBindingResult().getFieldErrors().stream()
                .map(ValidationExceptionDto::new)
                .collect(Collectors.toList());
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(collect);
    }

    private Map<String, Object> getErrorAttributes(WebRequest webRequest) {
        return new HashMap<>(errorAttributes.getErrorAttributes(webRequest,
            ErrorAttributeOptions.of(ErrorAttributeOptions.Include.MESSAGE)));
    }

    /**
     * Customize the response for UserHasNoFriendWithIdException.
     *
     * @param ex      the exception
     * @param request the current request
     * @return a {@code ResponseEntity} message
     */
    @ExceptionHandler(UserHasNoFriendWithIdException.class)
    public final ResponseEntity<Object> handleUserHasNoFriendWithIdException(
        UserHasNoFriendWithIdException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }

    /**
     * Customize the response for MultipartXSSProcessingException.
     *
     * @param ex      the exception
     * @param request the current request
     * @return a {@code ResponseEntity} message
     */
    @ExceptionHandler(MultipartXSSProcessingException.class)
    public final ResponseEntity<Object> handleMultipartXSSProcessingException(
        MultipartXSSProcessingException ex, WebRequest request) {
        ExceptionResponse exceptionResponse = new ExceptionResponse(getErrorAttributes(request));
        log.warn(ex.getMessage(), ex);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(exceptionResponse);
    }
}
