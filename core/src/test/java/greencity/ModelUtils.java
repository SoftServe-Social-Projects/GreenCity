package greencity;

import com.fasterxml.jackson.databind.ObjectMapper;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.PageableDto;
import greencity.dto.achievement.AchievementPostDto;
import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievement.ActionDto;
import greencity.dto.achievementcategory.AchievementCategoryDto;
import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.advice.AdvicePostDto;
import greencity.dto.comment.CommentAuthorDto;
import greencity.dto.comment.CommentDto;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.dto.econews.EcoNewsDto;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.AddressDto;
import greencity.dto.event.EventAuthorDto;
import greencity.dto.event.EventDateLocationDto;
import greencity.dto.event.EventDto;
import greencity.dto.event.UpdateEventDateLocationDto;
import greencity.dto.event.UpdateEventRequestDto;
import greencity.dto.favoriteplace.FavoritePlaceDto;
import greencity.dto.filter.FilterEventDto;
import greencity.dto.filter.FilterNotificationDto;
import greencity.dto.friends.UserAsFriendDto;
import greencity.dto.habit.CustomHabitDtoRequest;
import greencity.dto.habit.HabitAssignCustomPropertiesDto;
import greencity.dto.habit.HabitAssignPropertiesDto;
import greencity.dto.habit.UserShoppingAndCustomShoppingListsDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.language.LanguageDTO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.newssubscriber.NewsSubscriberRequestDto;
import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.ShoppingListItemPostDto;
import greencity.dto.shoppinglistitem.ShoppingListItemRequestDto;
import greencity.dto.tag.TagPostDto;
import greencity.dto.tag.TagTranslationVO;
import greencity.dto.tag.TagUaEnDto;
import greencity.dto.tag.TagVO;
import greencity.dto.tag.TagViewDto;
import greencity.dto.user.EcoNewsAuthorDto;
import greencity.dto.user.HabitIdRequestDto;
import greencity.dto.user.UserManagementDto;
import greencity.dto.user.UserShoppingListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.User;
import greencity.enums.EventStatus;
import greencity.enums.NotificationType;
import greencity.enums.ProjectName;
import greencity.enums.Role;
import greencity.enums.ShoppingListItemStatus;
import greencity.enums.TagType;
import greencity.enums.UserStatus;
import java.security.Principal;
import java.sql.Date;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

public class ModelUtils {
    public static List<TagTranslationVO> getTagTranslationsVO() {
        return Arrays.asList(TagTranslationVO.builder().id(1L).name("Новини").build(),
            TagTranslationVO.builder().id(2L).name("News").build(),
            TagTranslationVO.builder().id(3L).name("Новины").build());
    }

    public static TagVO getTagVO() {
        return new TagVO(1L, TagType.ECO_NEWS, getTagTranslationsVO(), null, null, null);
    }

    public static TagPostDto getTagPostDto() {
        return new TagPostDto(TagType.ECO_NEWS, Collections.emptyList());
    }

    public static TagViewDto getTagViewDto() {
        return new TagViewDto("3", "ECO_NEWS", "News");
    }

    public static PageableAdvancedDto<TagVO> getPageableAdvancedDtoForTag() {
        return new PageableAdvancedDto<>(Collections.singletonList(getTagVO()),
            10, 1, 4, 8,
            true, true, true, true);
    }

    public static User getUser() {
        return User.builder()
            .id(1L)
            .email(TestConst.EMAIL)
            .name(TestConst.NAME)
            .role(Role.ROLE_USER)
            .lastActivityTime(LocalDateTime.now())
            .dateOfRegistration(LocalDateTime.now())
            .build();
    }

    public static UserVO getUserVO() {
        return UserVO.builder()
            .id(1L)
            .email(TestConst.EMAIL)
            .name(TestConst.NAME)
            .role(Role.ROLE_USER)
            .build();
    }

    public static EcoNewsAuthorDto getEcoNewsAuthorDto() {
        return new EcoNewsAuthorDto(1L, TestConst.NAME);
    }

    public static EcoNewsDto getEcoNewsDto() {
        return new EcoNewsDto(ZonedDateTime.of(2022, 12, 12, 12, 12, 12, 12, ZoneId.systemDefault()), null, 1L,
            "title", "text", "shortInfo", getEcoNewsAuthorDto(), null, null, 12, 12, 12, false);
    }

    public static AddEcoNewsDtoRequest getAddEcoNewsDtoRequest() {
        return new AddEcoNewsDtoRequest("title", "text", Collections.singletonList("tag"), null, "shortInfo");
    }

    public static FavoritePlaceDto getFavoritePlaceDto() {
        return new FavoritePlaceDto("name", 3L);
    }

    public static LanguageDTO getLanguageDTO() {
        return new LanguageDTO(1L, "en");
    }

    public static LanguageTranslationDTO getLanguageTranslationDTO() {
        return new LanguageTranslationDTO(getLanguageDTO(), "content");
    }

    public static Principal getPrincipal() {
        return () -> "test@gmail.com";
    }

    public static NewsSubscriberRequestDto getNewsSubscriberRequestDto() {
        return new NewsSubscriberRequestDto("test@gmail.com");
    }

    public static ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    public static List<LanguageTranslationDTO> getLanguageTranslationsDTOs() {
        return Arrays.asList(
            new LanguageTranslationDTO(new LanguageDTO(1L, "en"), "hello"),
            new LanguageTranslationDTO(new LanguageDTO(1L, "en"), "text"),
            new LanguageTranslationDTO(new LanguageDTO(1L, "en"), "smile"));
    }

    public static AdvicePostDto getAdvicePostDto() {
        return new AdvicePostDto(getLanguageTranslationsDTOs(), new HabitIdRequestDto(1L));
    }

    public static ShoppingListItemPostDto getShoppingListItemPostDto() {
        return new ShoppingListItemPostDto(getLanguageTranslationsDTOs(), new ShoppingListItemRequestDto(1L));
    }

    public static AchievementCategoryDto getAchievementCategoryDto() {
        return new AchievementCategoryDto("name");
    }

    public static AchievementPostDto getAchievementPostDto() {
        return new AchievementPostDto("ACQUIRED_HABIT_14_DAYS", "Набуття звички протягом 14 днів",
            "Acquired habit 14 days", getAchievementCategoryDto(), 1);
    }

    public static AchievementVO getAchievementVO() {
        return new AchievementVO(1L, "ACQUIRED_HABIT_14_DAYS", "Набуття звички протягом 14 днів",
            "Acquired habit 14 days",
            new AchievementCategoryVO(1L, "name"), 1, 0);
    }

    public static UserManagementDto getUserManagementDto() {
        return UserManagementDto.builder()
            .id(1L)
            .name("Username")
            .email("user@gmail.com")
            .userCredo("Credo")
            .role(Role.ROLE_ADMIN)
            .userStatus(UserStatus.ACTIVATED)
            .build();
    }

    public static HabitAssignPropertiesDto getHabitAssignPropertiesDto() {
        return HabitAssignPropertiesDto.builder()
            .defaultShoppingListItems(List.of(1L, 2L))
            .duration(20)
            .build();
    }

    public static HabitAssignCustomPropertiesDto getHabitAssignCustomPropertiesDto() {
        return HabitAssignCustomPropertiesDto.builder()
            .habitAssignPropertiesDto(getHabitAssignPropertiesDto())
            .friendsIdsList(List.of(1L, 2L))
            .build();
    }

    public static CustomShoppingListItemResponseDto getCustomShoppingListItemResponseDto() {
        return CustomShoppingListItemResponseDto.builder()
            .id(1L)
            .status(ShoppingListItemStatus.ACTIVE)
            .text("text")
            .build();
    }

    public static UserShoppingListItemResponseDto getUserShoppingListItemResponseDto() {
        return UserShoppingListItemResponseDto.builder()
            .id(1L)
            .status(ShoppingListItemStatus.ACTIVE)
            .text("text")
            .build();
    }

    public static UserShoppingAndCustomShoppingListsDto getUserShoppingAndCustomShoppingListsDto() {
        return UserShoppingAndCustomShoppingListsDto.builder()
            .userShoppingListItemDto(List.of(getUserShoppingListItemResponseDto()))
            .customShoppingListItemDto(List.of(getCustomShoppingListItemResponseDto()))
            .build();
    }

    public static AddEventDtoRequest getEventDtoWithoutDates() {
        return AddEventDtoRequest.builder().title("Title").description("Desc").isOpen(true).build();
    }

    public static UpdateEventRequestDto getUpdateEventDtoWithoutDates() {
        return UpdateEventRequestDto.builder().title("Title").description("Desc").isOpen(true).build();
    }

    public static UpdateEventRequestDto getUpdateEventDto() {
        return UpdateEventRequestDto.builder().datesLocations(List.of(UpdateEventDateLocationDto.builder()
            .startDate(ZonedDateTime.now().plusDays(5))
            .finishDate(ZonedDateTime.now().plusDays(5).plusHours(1))
            .onlineLink("http://localhost:8060/swagger-ui.html#/")
            .build(),
            UpdateEventDateLocationDto.builder()
                .startDate(ZonedDateTime.now().plusDays(6))
                .finishDate(ZonedDateTime.now().plusDays(6).plusHours(1))
                .onlineLink("http://localhost:8060/swagger-ui.html#/")
                .build()))
            .tags(List.of("first", "second", "third")).build();
    }

    public static UpdateEventRequestDto getUpdateEventDtoWithTooManyDates() {
        List<UpdateEventDateLocationDto> eventDateLocationDtos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            eventDateLocationDtos.add(UpdateEventDateLocationDto.builder().onlineLink(String.valueOf(i)).build());
        }
        return UpdateEventRequestDto.builder().datesLocations(eventDateLocationDtos).build();
    }

    public static UpdateEventRequestDto getUpdateEventDtoWithEmptyDateLocations() {
        return UpdateEventRequestDto.builder().datesLocations(new ArrayList<>()).build();
    }

    public static UpdateEventRequestDto getUpdateEventWithoutAddressAndLink() {
        return UpdateEventRequestDto.builder().datesLocations(List.of(UpdateEventDateLocationDto.builder()
            .startDate(ZonedDateTime.now().plusDays(5))
            .finishDate(ZonedDateTime.now().plusDays(5).plusHours(1)).build())).build();
    }

    public static AddEventDtoRequest getEventDtoWithZeroDates() {
        return AddEventDtoRequest.builder().datesLocations(new ArrayList<>()).build();
    }

    public static AddEventDtoRequest getEventDtoWithTooManyDates() {
        List<EventDateLocationDto> eventDateLocationDtos = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            eventDateLocationDtos.add(EventDateLocationDto.builder().id((long) i).build());
        }
        return AddEventDtoRequest.builder().datesLocations(eventDateLocationDtos).build();
    }

    public static AddEventDtoRequest getEventWithPastStartDate() {
        return AddEventDtoRequest.builder().datesLocations(List.of(EventDateLocationDto.builder()
            .startDate(ZonedDateTime.of(LocalDateTime.of(2022, 1, 1, 0, 0), ZoneId.systemDefault()))
            .finishDate(ZonedDateTime.of(LocalDateTime.now(), ZoneId.systemDefault())).build())).build();
    }

    public static AddEventDtoRequest getEventWithStartDateAfterFinishDate() {
        return AddEventDtoRequest.builder().datesLocations(List.of(EventDateLocationDto.builder()
            .startDate(ZonedDateTime.of(LocalDateTime.of(2020, 1, 1, 0, 0), ZoneId.systemDefault()))
            .finishDate(ZonedDateTime.of(LocalDateTime.of(2019, 1, 1, 0, 0), ZoneId.systemDefault())).build())).build();
    }

    public static AddEventDtoRequest getEventWithoutAddressAndLink() {
        return AddEventDtoRequest.builder().datesLocations(List.of(EventDateLocationDto.builder()
            .startDate(ZonedDateTime.now().plusDays(5))
            .finishDate(ZonedDateTime.now().plusDays(5).plusHours(1)).build())).build();
    }

    public static AddEventDtoRequest getEventWithInvalidLink() {
        return AddEventDtoRequest.builder().datesLocations(List.of(EventDateLocationDto.builder()
            .startDate(ZonedDateTime.now().plusDays(5))
            .finishDate(ZonedDateTime.now().plusDays(5).plusHours(1)).onlineLink("invalidLink.").build())).build();
    }

    public static AddEventDtoRequest getEventWithTooManyTags() {
        return AddEventDtoRequest.builder().datesLocations(List.of(EventDateLocationDto.builder()
            .startDate(ZonedDateTime.now().plusDays(5))
            .finishDate(ZonedDateTime.now().plusDays(5).plusHours(1))
            .onlineLink("http://localhost:8060/swagger-ui.html#/").build()))
            .tags(List.of("first", "second", "third", "fourth")).build();
    }

    public static AddEventDtoRequest getAddEventDtoRequest() {
        return AddEventDtoRequest.builder().datesLocations(List.of(
            EventDateLocationDto.builder()
                .startDate(ZonedDateTime.now().plusDays(5))
                .finishDate(ZonedDateTime.now().plusDays(5).plusHours(1))
                .onlineLink("http://localhost:8060/swagger-ui.html#/")
                .build(),
            EventDateLocationDto.builder()
                .startDate(ZonedDateTime.now().plusDays(6))
                .finishDate(ZonedDateTime.now().plusDays(6).plusHours(1))
                .onlineLink("http://localhost:8060/swagger-ui.html#/")
                .build()))
            .tags(List.of("first", "second", "third")).build();
    }

    public static CustomHabitDtoRequest getAddCustomHabitDtoRequest() {
        return CustomHabitDtoRequest.builder()
            .complexity(2)
            .customShoppingListItemDto(List.of(
                CustomShoppingListItemResponseDto.builder()
                    .id(1L)
                    .status(ShoppingListItemStatus.ACTIVE)
                    .text("buy a shopper")
                    .build()))
            .defaultDuration(7)
            .habitTranslations(
                List.of(HabitTranslationDto.builder()
                    .description("Description")
                    .habitItem("Item")
                    .languageCode("en")
                    .name("use shopper")
                    .build()))
            .image("https://csb10032000a548f571.blob.core.windows.net/allfiles/photo_2021-06-01_15-39-56.jpg")
            .tagIds(Set.of(20L))
            .build();
    }

    public static FilterEventDto getFilterEventDto() {
        return FilterEventDto.builder()
            .statuses(List.of(EventStatus.JOINED))
            .build();
    }

    public static ActionDto getActionDto() {
        return ActionDto.builder().build();
    }

    public static Pageable getPageable() {
        return PageRequest.of(0, 20);
    }

    public static FilterNotificationDto getFilterNotificationDto() {
        return FilterNotificationDto.builder().projectName(new ProjectName[] {})
            .notificationType(new NotificationType[] {}).build();
    }

    public static PageableAdvancedDto<EventDto> getEventDtoPageableAdvancedDto(Pageable pageable) {
        return new PageableAdvancedDto<>(
            ModelUtils.getListEventDto(),
            2,
            pageable.getPageNumber(),
            1,
            0,
            false,
            false,
            true,
            true);
    }

    public static List<EventDto> getListEventDto() {
        return List.of(
            EventDto.builder()
                .id(3L)
                .title("test3")
                .organizer(EventAuthorDto.builder().id(2L).name("Test3").build())
                .creationDate(Date.valueOf("2024-04-14").toLocalDate())
                .dates(List.of(
                    EventDateLocationDto.builder()
                        .startDate(
                            ZonedDateTime.ofInstant(Instant.parse("2025-05-15T00:00:03Z"), ZoneId.systemDefault()))
                        .finishDate(
                            ZonedDateTime.ofInstant(Instant.parse("2025-05-16T00:00:03Z"), ZoneId.systemDefault()))
                        .onlineLink("testtesttesttest")
                        .coordinates(AddressDto.builder()
                            .latitude(0.0)
                            .longitude(1.0)
                            .cityEn("Kyiv")
                            .build())
                        .build()))
                .tags(List.of(TagUaEnDto.builder()
                    .id(2L)
                    .nameUa("Соціальний1")
                    .nameEn("Social1")
                    .build()))
                .titleImage("image.png")
                .isOpen(true)
                .isSubscribed(true)
                .isFavorite(true)
                .isRelevant(true)
                .likes(0)
                .countComments(2)
                .isOrganizedByFriend(false)
                .eventRate(3.5)
                .build(),
            EventDto.builder()
                .id(1L)
                .title("test1")
                .organizer(EventAuthorDto.builder().id(1L).name("Test").build())
                .creationDate(Date.valueOf("2024-04-16").toLocalDate())
                .dates(List.of(
                    EventDateLocationDto.builder()
                        .startDate(
                            ZonedDateTime.ofInstant(Instant.parse("2025-05-15T00:00:03Z"), ZoneId.systemDefault()))
                        .finishDate(
                            ZonedDateTime.ofInstant(Instant.parse("2025-05-16T00:00:03Z"), ZoneId.systemDefault()))
                        .onlineLink("testtesttesttest")
                        .coordinates(AddressDto.builder()
                            .latitude(0.0)
                            .longitude(1.0)
                            .cityEn("Kyiv")
                            .build())
                        .build()))
                .tags(List.of(TagUaEnDto.builder()
                    .id(1L)
                    .nameUa("Соціальний")
                    .nameEn("Social")
                    .build()))
                .titleImage("image.png")
                .isOpen(true)
                .isSubscribed(true)
                .isFavorite(true)
                .isRelevant(true)
                .likes(0)
                .countComments(2)
                .isOrganizedByFriend(false)
                .eventRate(3.5)
                .build());
    }

    public static UserAsFriendDto getUserAsFriendDto() {
        return UserAsFriendDto.builder()
            .id(1L)
            .requesterId(1L)
            .friendStatus("FRIEND")
            .chatId(1L)
            .build();
    }

    public static PageableDto<CommentDto> getPageableCommentDtos() {
        List<CommentDto> commentDtos = Stream.iterate(0, i -> i + 1)
            .limit(5)
            .map(i -> CommentDto.builder()
                .id((long) i)
                .text("Comment #" + i)
                .modifiedDate(LocalDateTime.now().minusDays(i))
                .author(CommentAuthorDto.builder()
                    .id(1L)
                    .name("UserName")
                    .profilePicturePath("PicturePath")
                    .build())
                .currentUserLiked(false)
                .build())
            .toList();

        return new PageableDto<>(
            commentDtos,
            commentDtos.size(),
            1,
            1);
    }
}