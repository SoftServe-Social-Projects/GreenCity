package greencity.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import greencity.ModelUtils;
import greencity.client.RestClient;
import greencity.dto.category.CategoryDto;
import greencity.dto.category.CategoryVO;
import greencity.dto.place.PlaceNotificationDto;
import greencity.dto.place.PlaceVO;
import greencity.dto.user.PlaceAuthorDto;
import greencity.dto.user.UserVO;
import greencity.entity.Category;
import greencity.entity.Place;
import greencity.enums.EmailNotification;
import greencity.message.SendReportEmailMessage;
import greencity.repository.PlaceRepo;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.modelmapper.ModelMapper;
import org.springframework.test.context.junit.jupiter.SpringExtension;

@ExtendWith(SpringExtension.class)
class NotificationServiceImplTest {

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @Mock
    private PlaceRepo placeRepo;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private RestClient restClient;

    @Test
    void sendImmediatelyReportTest() {
        EmailNotification emailNotification = EmailNotification.IMMEDIATELY;
        CategoryVO category = CategoryVO.builder()
            .id(12L)
            .name("category")
            .build();

        UserVO userVO = ModelUtils.getUserVO();
        userVO.setEmailNotification(emailNotification);

        PlaceVO place = new PlaceVO();
//        place.setLocationId(1L);
        place.setId(1L);
        place.setName("Forum");
        place.setDescription("Shopping center");
        place.setPhone("0322 489 850");
        place.setEmail("forum_lviv@gmail.com");
        place.setModifiedDate(ZonedDateTime.now());
        place.setCategory(category);

        when(restClient.findAllByEmailNotification(emailNotification))
            .thenReturn(Collections.singletonList(userVO));
        when(modelMapper.map(userVO, PlaceAuthorDto.class))
            .thenReturn(new PlaceAuthorDto(1L, "dto", "email"));
        when(modelMapper.map(place.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category", "test", null));
        when(modelMapper.map(place, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name", new CategoryDto("category", "test", null)));

        notificationService.sendImmediatelyReport(place);

        verify(restClient, Mockito.times(1))
            .sendReport(any(SendReportEmailMessage.class));
    }

    @Test
    void sendDailyReportTest() {
        EmailNotification emailNotification = EmailNotification.DAILY;
        Category category = ModelUtils.getCategory();
        UserVO userVO = ModelUtils.getUserVO();
        userVO.setEmailNotification(emailNotification);

        Place testPlace1 = ModelUtils.getPlace();
        testPlace1.setCategory(category);
        testPlace1.setId(1L);

        Place testPlace2 = ModelUtils.getPlace();
        testPlace1.setCategory(category);
        testPlace1.setId(2L);

        List<Place> testPlaces = Arrays.asList(testPlace1, testPlace2);

        when(restClient.findAllByEmailNotification(emailNotification))
            .thenReturn(Collections.singletonList(userVO));
        when(modelMapper.map(userVO, PlaceAuthorDto.class))
            .thenReturn(new PlaceAuthorDto(1L, "dto", "email"));
        when(placeRepo.findAllByModifiedDateBetweenAndStatus(any(LocalDateTime.class), any(LocalDateTime.class), any()))
            .thenReturn(testPlaces);
        when(modelMapper.map(testPlace1, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name", new CategoryDto("category", "test", null)));
        when(modelMapper.map(testPlace2, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name1", new CategoryDto("category1", "test", null)));
        when(modelMapper.map(testPlace1.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category", "test", null));
        when(modelMapper.map(testPlace2.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category1", "test", null));

        notificationService.sendDailyReport();

        verify(restClient, Mockito.times(1))
            .sendReport(any(SendReportEmailMessage.class));

    }

    @Test
    void sendWeeklyReportTest() {
        EmailNotification emailNotification = EmailNotification.WEEKLY;
        Category category = ModelUtils.getCategory();
        UserVO userVO = ModelUtils.getUserVO();
        userVO.setEmailNotification(emailNotification);

        Place testPlace1 = ModelUtils.getPlace();
        testPlace1.setCategory(category);
        testPlace1.setId(1L);

        Place testPlace2 = ModelUtils.getPlace();
        testPlace1.setCategory(category);
        testPlace1.setId(2L);

        List<Place> testPlaces = Arrays.asList(testPlace1, testPlace2);

        when(restClient.findAllByEmailNotification(emailNotification))
            .thenReturn(Collections.singletonList(userVO));
        when(modelMapper.map(userVO, PlaceAuthorDto.class))
            .thenReturn(new PlaceAuthorDto(1L, "dto", "email"));
        when(placeRepo.findAllByModifiedDateBetweenAndStatus(any(LocalDateTime.class), any(LocalDateTime.class), any()))
            .thenReturn(testPlaces);
        when(modelMapper.map(testPlace1, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name", new CategoryDto("category", "test", null)));
        when(modelMapper.map(testPlace2, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name1", new CategoryDto("category1", "test", null)));
        when(modelMapper.map(testPlace1.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category", "test", null));
        when(modelMapper.map(testPlace2.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category1", "test", null));

        notificationService.sendWeeklyReport();

        verify(restClient, Mockito.times(1))
            .sendReport(any(SendReportEmailMessage.class));

    }

    @Test
    void sendMonthlyReportTest() {
        EmailNotification emailNotification = EmailNotification.MONTHLY;
        Category category = ModelUtils.getCategory();
        UserVO userVO = ModelUtils.getUserVO();
        userVO.setEmailNotification(emailNotification);

        Place testPlace1 = ModelUtils.getPlace();
        testPlace1.setCategory(category);
        testPlace1.setId(1L);

        Place testPlace2 = ModelUtils.getPlace();
        testPlace1.setCategory(category);
        testPlace1.setId(2L);

        List<Place> testPlaces = Arrays.asList(testPlace1, testPlace2);

        when(restClient.findAllByEmailNotification(emailNotification))
            .thenReturn(Collections.singletonList(userVO));
        when(modelMapper.map(userVO, PlaceAuthorDto.class))
            .thenReturn(new PlaceAuthorDto(1L, "dto", "email"));
        when(placeRepo.findAllByModifiedDateBetweenAndStatus(any(LocalDateTime.class), any(LocalDateTime.class), any()))
            .thenReturn(testPlaces);
        when(modelMapper.map(testPlace1, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name", new CategoryDto("category", "test", null)));
        when(modelMapper.map(testPlace2, PlaceNotificationDto.class))
            .thenReturn(new PlaceNotificationDto("name1", new CategoryDto("category1", "test", null)));
        when(modelMapper.map(testPlace1.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category", "test", null));
        when(modelMapper.map(testPlace2.getCategory(), CategoryDto.class))
            .thenReturn(new CategoryDto("category1", "test", null));

        notificationService.sendMonthlyReport();

        verify(restClient, Mockito.times(1))
            .sendReport(any(SendReportEmailMessage.class));
    }
}