package greencity.service;

import greencity.client.RestClient;
import greencity.dto.econews.EcoNewsDto;
import greencity.dto.econews.InterestingEcoNewsDto;
import greencity.dto.econews.ShortEcoNewsDto;
import greencity.dto.subscription.SubscriptionRequestDto;
import greencity.dto.subscription.SubscriptionResponseDto;
import greencity.dto.user.SubscriberDto;
import greencity.dto.user.UserVO;
import greencity.entity.Subscription;
import greencity.enums.SubscriptionType;
import greencity.exception.exceptions.BadRequestException;
import greencity.exception.exceptions.NotFoundException;
import greencity.repository.SubscriptionRepo;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import static greencity.ModelUtils.getEcoNewsDto;
import static greencity.ModelUtils.getShortEcoNewsDto;
import static greencity.ModelUtils.getSubscriberDto;
import static greencity.ModelUtils.getUserVO;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceImplTest {

    @Mock
    private SubscriptionRepo subscriptionRepo;

    @Mock
    private EcoNewsService ecoNewsService;

    @Mock
    private UserService userService;

    @Mock
    private RestClient restClient;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    private SubscriptionRequestDto subscriptionRequestDto;
    private Subscription subscription;
    private SubscriptionResponseDto subscriptionResponseDto;
    private UUID unsubscribeToken;

    @BeforeEach
    void setUp() {
        subscriptionRequestDto = new SubscriptionRequestDto();
        subscriptionRequestDto.setEmail("test@example.com");
        subscriptionRequestDto.setSubscriptionType(SubscriptionType.ECO_NEWS);

        subscription = new Subscription();
        subscription.setEmail("test@example.com");
        subscription.setSubscriptionType(SubscriptionType.ECO_NEWS);
        subscription.setUnsubscribeToken(UUID.randomUUID());

        subscriptionResponseDto = new SubscriptionResponseDto();
        subscriptionResponseDto.setUnsubscribeToken(subscription.getUnsubscribeToken());

        unsubscribeToken = UUID.randomUUID();
    }

    @Test
    void testCreateSubscription() {
        when(subscriptionRepo.existsByEmailAndSubscriptionType("test@example.com", SubscriptionType.ECO_NEWS))
            .thenReturn(false);
        when(modelMapper.map(subscriptionRequestDto, Subscription.class)).thenReturn(subscription);
        when(subscriptionRepo.save(any(Subscription.class))).thenReturn(subscription);
        when(modelMapper.map(subscription, SubscriptionResponseDto.class)).thenReturn(subscriptionResponseDto);

        SubscriptionResponseDto result = subscriptionService.createSubscription(subscriptionRequestDto);

        assertEquals(subscriptionResponseDto, result);
        verify(subscriptionRepo).save(any(Subscription.class));
    }

    @Test
    void testCreateSubscriptionAlreadyExists() {
        when(subscriptionRepo.existsByEmailAndSubscriptionType("test@example.com", SubscriptionType.ECO_NEWS))
            .thenReturn(true);

        assertThrows(BadRequestException.class, () -> subscriptionService.createSubscription(subscriptionRequestDto));
    }

    @Test
    void testDeleteSubscription() {
        when(subscriptionRepo.existsByUnsubscribeToken(unsubscribeToken)).thenReturn(true);

        subscriptionService.deleteSubscription(unsubscribeToken);

        verify(subscriptionRepo).deleteByUnsubscribeToken(unsubscribeToken);
    }

    @Test
    void testDeleteSubscriptionNotFound() {
        when(subscriptionRepo.existsByUnsubscribeToken(unsubscribeToken)).thenReturn(false);

        assertThrows(NotFoundException.class, () -> subscriptionService.deleteSubscription(unsubscribeToken));
    }

    @Test
    void testSendContentToSubscribers() {
        EcoNewsDto ecoNewsDto = getEcoNewsDto();
        ShortEcoNewsDto shortEcoNewsDto = getShortEcoNewsDto();
        UserVO userVO = getUserVO();
        SubscriberDto subscriberDto = getSubscriberDto();

        List<EcoNewsDto> ecoNewsList = List.of(ecoNewsDto, ecoNewsDto);
        when(ecoNewsService.getThreeInterestingEcoNews()).thenReturn(ecoNewsList);
        when(modelMapper.map(ecoNewsDto, ShortEcoNewsDto.class)).thenReturn(shortEcoNewsDto);

        Page<Subscription> subscriptionsPage = new PageImpl<>(Collections.singletonList(subscription));
        when(subscriptionRepo.findAll(any(PageRequest.class))).thenReturn(subscriptionsPage);

        List<UserVO> users = List.of(userVO);
        when(userService.findByEmails(anyList())).thenReturn(users);
        when(modelMapper.map(subscription, SubscriberDto.class)).thenReturn(subscriberDto);

        subscriptionService.sendContentToSubscribers();

        verify(restClient).sendInterestingEcoNews(any(InterestingEcoNewsDto.class));
    }

    @Test
    void testSendContentToSubscribersNoEcoNews() {
        when(ecoNewsService.getThreeInterestingEcoNews()).thenReturn(List.of());

        subscriptionService.sendContentToSubscribers();

        verify(subscriptionRepo, never()).findAll(any(PageRequest.class));
        verify(restClient, never()).sendInterestingEcoNews(any(InterestingEcoNewsDto.class));
    }

}