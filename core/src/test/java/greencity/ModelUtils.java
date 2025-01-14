package greencity;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.maps.model.LatLng;
import com.google.maps.model.PriceLevel;
import com.google.maps.model.RankBy;
import greencity.constant.AppConstant;
import greencity.dto.PageableAdvancedDto;
import greencity.dto.achievement.AchievementPostDto;

import greencity.dto.achievement.AchievementVO;
import greencity.dto.achievementcategory.AchievementCategoryDto;
import greencity.dto.achievementcategory.AchievementCategoryVO;
import greencity.dto.advice.AdvicePostDto;
import greencity.dto.breaktime.BreakTimeDto;
import greencity.dto.category.CategoryVO;
import greencity.dto.discount.DiscountValueDto;
import greencity.dto.econews.AddEcoNewsDtoRequest;
import greencity.dto.econews.AddEcoNewsDtoResponse;
import greencity.dto.econews.EcoNewsDto;
import greencity.dto.econewscomment.AddEcoNewsCommentDtoRequest;
import greencity.dto.econewscomment.AddEcoNewsCommentDtoResponse;
import greencity.dto.econewscomment.EcoNewsCommentAuthorDto;
import greencity.dto.econewscomment.EcoNewsCommentDto;
import greencity.dto.event.AddEventDtoRequest;
import greencity.dto.event.EventDateLocationDto;
import greencity.dto.factoftheday.FactOfTheDayDTO;
import greencity.dto.factoftheday.FactOfTheDayPostDTO;
import greencity.dto.factoftheday.FactOfTheDayTranslationEmbeddedPostDTO;
import greencity.dto.favoriteplace.FavoritePlaceDto;
import greencity.dto.filter.FilterEventDto;
import greencity.dto.filter.FilterPlacesApiDto;
import greencity.dto.habit.HabitAssignCustomPropertiesDto;
import greencity.dto.habit.HabitAssignPropertiesDto;
import greencity.dto.habit.HabitVO;
import greencity.dto.habit.UpdateUserShoppingListDto;
import greencity.dto.habit.CustomHabitDtoRequest;
import greencity.dto.habit.UserShoppingAndCustomShoppingListsDto;
import greencity.dto.habitfact.HabitFactPostDto;
import greencity.dto.habitfact.HabitFactTranslationUpdateDto;
import greencity.dto.habitfact.HabitFactTranslationVO;
import greencity.dto.habitfact.HabitFactUpdateDto;
import greencity.dto.habitfact.HabitFactVO;
import greencity.dto.habitstatistic.AddHabitStatisticDto;
import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.dto.language.LanguageDTO;
import greencity.dto.language.LanguageTranslationDTO;
import greencity.dto.language.LanguageVO;
import greencity.dto.location.LocationAddressAndGeoDto;
import greencity.dto.location.LocationDto;
import greencity.dto.location.LocationVO;
import greencity.dto.location.UserLocationDto;
import greencity.dto.newssubscriber.NewsSubscriberRequestDto;
import greencity.dto.openhours.OpeningHoursDto;
import greencity.dto.place.PlaceByBoundsDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.shoppinglistitem.CustomShoppingListItemResponseDto;
import greencity.dto.shoppinglistitem.ShoppingListItemPostDto;
import greencity.dto.shoppinglistitem.ShoppingListItemRequestDto;
import greencity.dto.tag.TagPostDto;
import greencity.dto.tag.TagTranslationVO;
import greencity.dto.tag.TagVO;
import greencity.dto.tag.TagViewDto;
import greencity.dto.user.EcoNewsAuthorDto;
import greencity.dto.user.HabitIdRequestDto;
import greencity.dto.user.UserManagementDto;
import greencity.dto.user.UserProfilePictureDto;
import greencity.dto.user.UserShoppingListItemAdvanceDto;
import greencity.dto.user.UserShoppingListItemResponseDto;
import greencity.dto.user.UserVO;
import greencity.entity.Advice;
import greencity.entity.BreakTime;
import greencity.entity.Category;
import greencity.entity.DiscountValue;
import greencity.entity.EcoNews;
import greencity.entity.EcoNewsComment;
import greencity.entity.FactOfTheDay;
import greencity.entity.FavoritePlace;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitFact;
import greencity.entity.HabitFactTranslation;
import greencity.entity.HabitStatistic;
import greencity.entity.HabitStatusCalendar;
import greencity.entity.HabitTranslation;
import greencity.entity.Language;
import greencity.entity.Location;
import greencity.entity.OpeningHours;
import greencity.entity.Photo;
import greencity.entity.Place;
import greencity.entity.ShoppingListItem;
import greencity.entity.Specification;
import greencity.entity.Tag;
import greencity.entity.User;
import greencity.entity.UserShoppingListItem;
import greencity.entity.localization.AdviceTranslation;
import greencity.entity.localization.ShoppingListItemTranslation;
import greencity.enums.CommentStatus;
import greencity.enums.FactOfDayStatus;
import greencity.enums.HabitAssignStatus;
import greencity.enums.HabitRate;
import greencity.enums.Role;
import greencity.enums.ShoppingListItemStatus;
import greencity.enums.TagType;
import greencity.enums.UserStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class ModelUtils {
    public static Tag getTag() {
        return new Tag(1L, TagType.ECO_NEWS, Collections.emptyList(), Collections.emptyList(),
            Collections.emptySet(), Collections.emptySet());
    }

    public static List<TagTranslationVO> getTagTranslationsVO() {
        return Arrays.asList(TagTranslationVO.builder().id(1L).name("Новини").build(),
            TagTranslationVO.builder().id(2L).name("News").build(),
            TagTranslationVO.builder().id(3L).name("Новины").build());
    }

    public static LanguageVO getLanguageVO() {
        return new LanguageVO(1L, AppConstant.DEFAULT_LANGUAGE_CODE);
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
            .userStatus(UserStatus.ACTIVATED)
            .userLocationDto(
                UserLocationDto.builder()
                    .latitude(1d)
                    .longitude(1d)
                    .build())
            .build();
    }

    public static EcoNewsAuthorDto getEcoNewsAuthorDto() {
        return new EcoNewsAuthorDto(1L, TestConst.NAME);
    }

    public static Language getLanguage() {
        return new Language(1L, AppConstant.DEFAULT_LANGUAGE_CODE, Collections.emptyList(), Collections.emptyList(),
            Collections.emptyList(), Collections.emptyList());
    }

    public static EcoNews getEcoNews() {
        return new EcoNews(1L, ZonedDateTime.now(), TestConst.SITE, null, "shortInfo", getUser(),
            "title", "text", null, Collections.singletonList(getTag()), null, null);
    }

    public static EcoNewsDto getEcoNewsDto() {
        return new EcoNewsDto(ZonedDateTime.of(2022, 12, 12, 12, 12, 12, 12, ZoneId.systemDefault()), null, 1L,
            "title", "text", "shortInfo", getEcoNewsAuthorDto(), null, null, 12, 12, 12);
    }

    public static AddEcoNewsDtoRequest getAddEcoNewsDtoRequest() {
        return new AddEcoNewsDtoRequest("title", "text",
            Collections.singletonList("tag"), null, null, "shortInfo");
    }

    public static AddEcoNewsDtoResponse getAddEcoNewsDtoResponse() {
        return new AddEcoNewsDtoResponse(1L, "title",
            "text", "shortInfo", EcoNewsAuthorDto.builder().id(1L).name(TestConst.NAME).build(),
            ZonedDateTime.now(), TestConst.SITE, null,
            Collections.singletonList("tag"));
    }

    public static MultipartFile getFile() {
        Path path = Paths.get("src/test/resources/test.jpg");
        String name = TestConst.IMG_NAME;
        String contentType = "photo/plain";
        byte[] content = null;
        try {
            content = Files.readAllBytes(path);
        } catch (final IOException e) {
            e.printStackTrace();
        }
        return new MockMultipartFile(name,
            name, contentType, content);
    }

    public static URL getUrl() throws MalformedURLException {
        return new URL(TestConst.SITE);
    }

    public static DiscountValue getDiscountValue() {
        return new DiscountValue(null, 33, null, null);
    }

    public static DiscountValueDto getDiscountValueDto() {
        return new DiscountValueDto(33, null);
    }

    public static Place getPlace() {
        Place place = new Place();
        place.setLocation(new Location(1L, 49.84988, 24.022533, "вулиця Під Дубом, 7Б", "test", place));
        place.setId(1L);
        place.setName("Forum");
        place.setDescription("Shopping center");
        place.setPhone("0322 489 850");
        place.setEmail("forum_lviv@gmail.com");
        place.setAuthor(getUser());
        place.setModifiedDate(ZonedDateTime.now());
        return place;
    }

    public static FavoritePlace getFavoritePlace() {
        return new FavoritePlace(3L, "name", getUser(), getPlace());
    }

    public static FavoritePlaceDto getFavoritePlaceDto() {
        return new FavoritePlaceDto("name", 3L);
    }

    public static AddHabitStatisticDto addHabitStatisticDto() {
        return AddHabitStatisticDto.builder()
            .amountOfItems(5)
            .habitRate(HabitRate.DEFAULT)
            .createDate(ZonedDateTime.now())
            .build();
    }

    public static HabitStatistic getHabitStatistic() {
        return HabitStatistic.builder()
            .id(1L)
            .habitRate(HabitRate.DEFAULT)
            .createDate(ZonedDateTime.now())
            .amountOfItems(5)
            .habitAssign(HabitAssign.builder().id(13L).build())
            .build();
    }

    public static Category getCategory() {
        return Category.builder()
            .id(12L)
            .name("category")
            .build();
    }

    public static ShoppingListItemTranslation getShoppingListItemTranslation() {
        return ShoppingListItemTranslation.builder()
            .id(2L)
            .language(
                new Language(2L, AppConstant.DEFAULT_LANGUAGE_CODE, Collections.emptyList(), Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList()))
            .shoppingListItem(
                new ShoppingListItem(1L, Collections.emptyList(), Collections.emptySet(), Collections.emptyList()))
            .content("Buy a bamboo toothbrush")
            .build();
    }

    public static List<ShoppingListItemTranslation> getShoppingListItemTranslations() {
        return Arrays.asList(
            ShoppingListItemTranslation.builder()
                .id(2L)
                .language(new Language(1L, AppConstant.DEFAULT_LANGUAGE_CODE, Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList()))
                .content("Buy a bamboo toothbrush")
                .shoppingListItem(
                    new ShoppingListItem(1L, Collections.emptyList(), Collections.emptySet(), Collections.emptyList()))
                .build(),
            ShoppingListItemTranslation.builder()
                .id(11L)
                .language(new Language(1L, AppConstant.DEFAULT_LANGUAGE_CODE, Collections.emptyList(),
                    Collections.emptyList(), Collections.emptyList(), Collections.emptyList()))
                .content("Start recycling batteries")
                .shoppingListItem(
                    new ShoppingListItem(4L, Collections.emptyList(), Collections.emptySet(), Collections.emptyList()))
                .build());
    }

    public static Advice getAdvice() {
        return new Advice(1L, null, null);
    }

    public static HabitFact getHabitFact() {
        return new HabitFact(1L, Collections.singletonList(getHabitFactTranslation()), null);
    }

    public static LocationAddressAndGeoDto getLocationAddressAndGeoDto() {
        return LocationAddressAndGeoDto.builder()
            .address("address")
            .lat(12.12d)
            .lng(12.12d)
            .build();
    }

    public static LocalTime getLocalTime() {
        return LocalTime.of(7, 20, 45, 342123342);
    }

    public static OpeningHoursDto getOpeningHoursDto() {
        OpeningHoursDto openingHours = new OpeningHoursDto();
        openingHours.setOpenTime(getLocalTime());
        openingHours.setCloseTime(getLocalTime());
        openingHours.setBreakTime(BreakTimeDto.builder()
            .startTime(getLocalTime())
            .endTime(getLocalTime())
            .build());
        openingHours.setWeekDay(DayOfWeek.MONDAY);
        return openingHours;
    }

    public static OpeningHours getOpeningHours() {
        OpeningHours openingHoursTest = new OpeningHours();
        openingHoursTest.setOpenTime(getLocalTime());
        openingHoursTest.setCloseTime(getLocalTime());
        openingHoursTest.setBreakTime(BreakTime.builder()
            .startTime(getLocalTime())
            .endTime(getLocalTime())
            .build());
        openingHoursTest.setWeekDay(DayOfWeek.MONDAY);
        return openingHoursTest;
    }

    public static Location getLocation() {
        return Location.builder()
            .address("address")
            .lng(12.12d)
            .lat(12.12d)
            .build();
    }

    public static LocationVO getLocationVO() {
        return LocationVO.builder()
            .address("address")
            .lng(12.12d)
            .lat(12.12d)
            .build();
    }

    public static Specification getSpecification() {
        return Specification.builder()
            .id(1L)
            .name("specification")
            .build();
    }

    public static Photo getPhoto() {
        return Photo.builder()
            .id(1L)
            .name("photo")
            .build();
    }

    public static LanguageDTO getLanguageDTO() {
        return new LanguageDTO(1L, "en");
    }

    public static LanguageTranslationDTO getLanguageTranslationDTO() {
        return new LanguageTranslationDTO(getLanguageDTO(), "content");
    }

    public static EcoNewsComment getEcoNewsComment() {
        return EcoNewsComment.builder()
            .id(1L)
            .text("text")
            .createdDate(LocalDateTime.now())
            .modifiedDate(LocalDateTime.now())
            .status(CommentStatus.ORIGINAL)
            .user(getUser())
            .ecoNews(getEcoNews())
            .build();
    }

    public static AddEcoNewsCommentDtoRequest getAddEcoNewsCommentDtoRequest() {
        return new AddEcoNewsCommentDtoRequest("text", 0L);
    }

    public static AddEcoNewsCommentDtoResponse getAddEcoNewsCommentDtoResponse() {
        return AddEcoNewsCommentDtoResponse.builder()
            .id(getEcoNewsComment().getId())
            .author(getEcoNewsCommentAuthorDto())
            .text(getEcoNewsComment().getText())
            .modifiedDate(getEcoNewsComment().getModifiedDate())
            .build();
    }

    public static EcoNewsCommentAuthorDto getEcoNewsCommentAuthorDto() {
        return EcoNewsCommentAuthorDto.builder()
            .id(getUser().getId())
            .name(getUser().getName().trim())
            .userProfilePicturePath(getUser().getProfilePicturePath())
            .build();
    }

    public static EcoNewsCommentDto getEcoNewsCommentDto() {
        return EcoNewsCommentDto.builder()
            .id(1L)
            .modifiedDate(LocalDateTime.now())
            .author(getEcoNewsCommentAuthorDto())
            .text("text")
            .replies(0)
            .likes(0)
            .currentUserLiked(false)
            .status(CommentStatus.ORIGINAL)
            .build();
    }

    public static Principal getPrincipal() {
        return () -> "test@gmail.com";
    }

    public static Principal getPrincipalForUserVO() {
        return () -> "taras@gmail.com";
    }

    public static NewsSubscriberRequestDto getNewsSubscriberRequestDto() {
        return new NewsSubscriberRequestDto("test@gmail.com");
    }

    public static UserProfilePictureDto getUserProfilePictureDto() {
        return new UserProfilePictureDto(1L, "name", "image");
    }

    public static FactOfTheDayDTO getFactOfTheDayDto() {
        return new FactOfTheDayDTO(1L, "name", null, ZonedDateTime.now());
    }

    public static FactOfTheDay getFactOfTheDay() {
        return new FactOfTheDay(1L, "Fact of the day", null, ZonedDateTime.now());
    }

    public static FactOfTheDayPostDTO getFactOfTheDayPostDto() {
        return new FactOfTheDayPostDTO(1L, "name",
            Collections.singletonList(
                new FactOfTheDayTranslationEmbeddedPostDTO("content", AppConstant.DEFAULT_LANGUAGE_CODE)));
    }

    public static ObjectMapper getObjectMapper() {
        return new ObjectMapper();
    }

    public static HabitAssign getHabitAssign() {
        return HabitAssign.builder()
            .id(1L)
            .status(HabitAssignStatus.ACQUIRED)
            .createDate(ZonedDateTime.now())
            .habit(Habit.builder()
                .id(1L)
                .image("")
                .habitTranslations(Collections.singletonList(HabitTranslation.builder()
                    .id(1L)
                    .name("")
                    .description("")
                    .habitItem("")
                    .language(getLanguage())
                    .build()))
                .build())
            .user(getUser())
            .workingDays(0)
            .duration(0)
            .habitStreak(0)
            .lastEnrollmentDate(ZonedDateTime.now())
            .build();
    }

    public static HabitStatusCalendar getHabitStatusCalendar() {
        return HabitStatusCalendar.builder()
            .id(1L)
            .enrollDate(LocalDate.now()).build();
    }

    public static AdviceTranslation getAdviceTranslation() {
        return AdviceTranslation.builder()
            .id(1L)
            .language(getLanguage())
            .content("Text content")
            .advice(getAdvice())
            .build();
    }

    public static HabitFactTranslation getHabitFactTranslation() {
        return HabitFactTranslation.builder()
            .id(1L)
            .content("content")
            .language(getLanguage())
            .factOfDayStatus(FactOfDayStatus.POTENTIAL)
            .habitFact(getHabitFact())
            .build();
    }

    public static HabitFactVO getHabitFactVO() {
        return HabitFactVO.builder()
            .id(1L)
            .habit(HabitVO.builder()
                .id(1L)
                .image("string")
                .build())
            .translations(Collections.singletonList(HabitFactTranslationVO.builder()
                .id(1L)
                .content("content")
                .factOfDayStatus(FactOfDayStatus.POTENTIAL)
                .habitFact(null)
                .language(LanguageVO.builder()
                    .id(1L)
                    .code("ua")
                    .build())
                .build()))
            .build();
    }

    public static PlaceVO getPlaceVO() {
        PlaceVO placeVO = new PlaceVO();
        placeVO.setLocation(getLocationVO());
        placeVO.setId(1L);
        placeVO.setName("Forum");
        placeVO.setDescription("Shopping center");
        placeVO.setPhone("0322 489 850");
        placeVO.setEmail("forum_lviv@gmail.com");
        placeVO.setAuthor(getUserVO());
        placeVO.setModifiedDate(ZonedDateTime.now());
        CategoryVO categoryVO = new CategoryVO();
        categoryVO.setName("category");
        placeVO.setCategory(categoryVO);
        return placeVO;
    }

    public static HabitFactPostDto getHabitFactPostDto() {
        return HabitFactPostDto.builder()
            .translations(List.of(getLanguageTranslationDTO()))
            .habit(new HabitIdRequestDto(1L))
            .build();
    }

    public static HabitFactUpdateDto getHabitFactUpdateDto() {
        return HabitFactUpdateDto.builder()
            .habit(new HabitIdRequestDto(1L))
            .translations(Collections.singletonList(
                new HabitFactTranslationUpdateDto(FactOfDayStatus.POTENTIAL, getLanguageDTO(), "")))
            .build();
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
            new AchievementCategoryVO(1L, "name"), 1);
    }

    public static UserShoppingListItem getUserShoppingListItem() {
        return UserShoppingListItem.builder()
            .id(1L)
            .status(ShoppingListItemStatus.DONE)
            .habitAssign(HabitAssign.builder()
                .id(1L)
                .status(HabitAssignStatus.ACQUIRED)
                .habitStreak(10)
                .duration(300)
                .lastEnrollmentDate(ZonedDateTime.now())
                .workingDays(5)
                .build())
            .shoppingListItem(ShoppingListItem.builder()
                .id(1L)
                .build())
            .dateCompleted(LocalDateTime.of(2021, 2, 2, 14, 2))
            .build();
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

    public static UpdateUserShoppingListDto getUpdateUserShoppingListDto() {
        return UpdateUserShoppingListDto.builder()
            .userShoppingListItemId(1L)
            .habitAssignId(1L)
            .userShoppingListAdvanceDto(List.of(UserShoppingListItemAdvanceDto.builder()
                .id(1L)
                .shoppingListItemId(1L)
                .status(ShoppingListItemStatus.INPROGRESS)
                .build()))
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
            .finishDate(ZonedDateTime.now().plusDays(5).plusHours(1)).onlineLink("invalidLink").build())).build();
    }

    public static AddEventDtoRequest getEventWithTooManyTags() {
        return AddEventDtoRequest.builder().datesLocations(List.of(EventDateLocationDto.builder()
            .startDate(ZonedDateTime.now().plusDays(5))
            .finishDate(ZonedDateTime.now().plusDays(5).plusHours(1))
            .onlineLink("http://localhost:8060/swagger-ui.html#/").build()))
            .tags(List.of("first", "second", "third", "fourth")).build();
    }

    public static AddEventDtoRequest getAddEventDtoRequest() {
        return AddEventDtoRequest.builder().datesLocations(List.of(EventDateLocationDto.builder()
            .startDate(ZonedDateTime.now().plusDays(5))
            .finishDate(ZonedDateTime.now().plusDays(5).plusHours(1))
            .onlineLink("http://localhost:8060/swagger-ui.html#/")
            .build())).tags(List.of("first", "second", "third")).build();
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

    public static FilterEventDto getNullFilterEventDto() {
        return FilterEventDto.builder()
            .eventTime(null)
            .cities(null)
            .statuses(null)
            .tags(null)
            .build();
    }

    public static FilterPlacesApiDto getFilterPlacesApiDto() {
        return FilterPlacesApiDto.builder()
            .location(new LatLng(0d, 0d))
            .radius(10000)
            .keyword("test")
            .rankBy(RankBy.PROMINENCE)
            .openNow(true)
            .minPrice(PriceLevel.FREE)
            .maxPrice(PriceLevel.VERY_EXPENSIVE)
            .build();
    }

    public static List<PlaceByBoundsDto> getPlaceByBoundsDto() {
        return List.of(PlaceByBoundsDto.builder()
            .id(1L)
            .name("testx")
            .location(new LocationDto())
            .build());
    }
}
