package greencity;

import greencity.dto.category.CategoryDto;
import greencity.dto.econews.AddEcoNewsDtoResponse;
import greencity.dto.econews.InterestingEcoNewsDto;
import greencity.dto.econews.ShortEcoNewsDto;
import greencity.dto.event.EventDto;
import greencity.dto.habit.CustomHabitDtoRequest;
import greencity.dto.habit.CustomHabitDtoResponse;
import greencity.dto.place.PlaceNotificationDto;
import greencity.dto.todolistitem.CustomToDoListItemRequestDto;
import greencity.dto.todolistitem.CustomToDoListItemResponseDto;
import greencity.dto.tag.TagUaEnDto;
import greencity.dto.user.EcoNewsAuthorDto;
import greencity.dto.user.SubscriberDto;
import greencity.dto.user.UserToDoListItemResponseDto;
import greencity.dto.user.UserStatusDto;
import greencity.dto.user.UserVO;
import greencity.dto.verifyemail.VerifyEmailVO;
import greencity.enums.EmailPreferencePeriodicity;
import greencity.enums.Role;
import greencity.enums.ToDoListItemStatus;
import greencity.enums.UserToDoListItemStatus;
import greencity.message.ScheduledEmailMessage;
import greencity.message.SendReportEmailMessage;
import greencity.message.SendHabitNotification;
import java.util.UUID;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static greencity.TestConst.ACCESS_TOKEN;
import static greencity.TestConst.USER_ID;
import static greencity.enums.UserStatus.ACTIVATED;

public class ModelUtils {
    public static UserVO getUserVO() {
        return UserVO.builder()
            .id(1L)
            .email(TestConst.EMAIL)
            .name(TestConst.NAME)
            .role(Role.ROLE_USER)
            .lastActivityTime(LocalDateTime.now())
            .verifyEmail(new VerifyEmailVO())
            .dateOfRegistration(LocalDateTime.now())
            .build();
    }

    public static PlaceNotificationDto getPlaceNotificationDto() {
        return PlaceNotificationDto.builder()
            .category(getCategoryDto())
            .name("name")
            .build();
    }

    public static SendHabitNotification getSendHabitNotification() {
        return SendHabitNotification.builder()
            .email("test@gmail.com")
            .name("taras")
            .build();
    }

    public static HttpHeaders getHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer accessToken");
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    public static CategoryDto getCategoryDto() {
        return CategoryDto.builder()
            .name("name")
            .parentCategoryId(1L)
            .build();
    }

    public static SendReportEmailMessage getSendReportEmailMessage() {
        return SendReportEmailMessage.builder()
            .periodicity(EmailPreferencePeriodicity.WEEKLY)
            .categoriesDtoWithPlacesDtoMap(Collections.singletonMap(
                getCategoryDto(), Collections.singletonList(getPlaceNotificationDto())))
            .subscribers(getSubscribers())
            .build();
    }

    public static AddEcoNewsDtoResponse getAddEcoNewsDtoResponse() {
        return new AddEcoNewsDtoResponse(1L, "title",
            "text", "shortInfo", EcoNewsAuthorDto.builder().id(1L).name(TestConst.NAME).build(),
            ZonedDateTime.now(), TestConst.SITE, null,
            Arrays.asList("Новини", "News", "Новины"));
    }

    public static InterestingEcoNewsDto getInterestingEcoNewsDto() {
        return InterestingEcoNewsDto.builder()
            .ecoNewsList(getShortEcoNewsDto())
            .subscribers(getSubscribers())
            .build();
    }

    private static List<SubscriberDto> getSubscribers() {
        return List.of(SubscriberDto.builder()
            .email("email@gmail.com")
            .name("Ilia")
            .unsubscribeToken(UUID.randomUUID())
            .build());
    }

    private static List<ShortEcoNewsDto> getShortEcoNewsDto() {
        return List.of(ShortEcoNewsDto.builder()
            .ecoNewsId(1L)
            .imagePath("https://google.com")
            .text("Text")
            .title("Title")
            .build());
    }

    public static TagUaEnDto tagUaEnDto = TagUaEnDto.builder().id(1L).nameUa("Сщціальний").nameEn("Social").build();

    public static EventDto getEventDtoWithTag() {
        return EventDto.builder()
            .id(1L)
            .countComments(2)
            .likes(20)
            .tags(List.of(tagUaEnDto))
            .build();
    }

    public static EventDto getEventDtoWithoutTag() {
        return EventDto.builder()
            .id(1L)
            .countComments(2)
            .likes(20)
            .build();
    }

    public static UserToDoListItemResponseDto getUserToDoListItemResponseDto() {
        return UserToDoListItemResponseDto.builder()
            .id(1L)
            .text("text")
            .status(UserToDoListItemStatus.INPROGRESS)
            .targetId(1L)
            .isCustomItem(true)
            .build();
    }

    public static CustomToDoListItemResponseDto getCustomToDoListItemResponseDto() {
        return CustomToDoListItemResponseDto.builder()
            .id(1L)
            .text("text")
            .build();
    }

    public static CustomToDoListItemRequestDto getCustomToDoListItemRequestDto() {
        return CustomToDoListItemRequestDto.builder()
            .id(1L)
            .text("text")
            .status(ToDoListItemStatus.ACTIVE.toString())
            .build();
    }

    public static CustomHabitDtoRequest getAddCustomHabitDtoRequest() {
        return CustomHabitDtoRequest.builder()
            .complexity(1)
            .image("")
            .defaultDuration(14)
            .tagIds(Set.of(20L))
            .build();
    }

    public static CustomHabitDtoResponse getAddCustomHabitDtoResponse() {
        return CustomHabitDtoResponse.builder()
            .id(1L)
            .complexity(1)
            .image("")
            .defaultDuration(14)
            .tagIds(Set.of(20L))
            .build();
    }

    public static URL getUrl() throws MalformedURLException {
        return new URL(TestConst.SITE);
    }

    public static ScheduledEmailMessage getScheduledEmailMessage() {
        return ScheduledEmailMessage.builder()
            .username("test")
            .body("test")
            .subject("test")
            .language("en")
            .baseLink("test")
            .email("test@gmail.com")
            .build();
    }

    public static UserStatusDto getUserStatusDto() {
        return UserStatusDto.builder().id(USER_ID).userStatus(ACTIVATED).build();
    }

    public static HttpEntity<UserStatusDto> getEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.set(HttpHeaders.AUTHORIZATION, ACCESS_TOKEN);
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(getUserStatusDto(), headers);
    }
}
