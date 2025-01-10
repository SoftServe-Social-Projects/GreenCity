package greencity.controller;

import greencity.dto.subscription.SubscriptionRequestDto;
import greencity.dto.subscription.SubscriptionResponseDto;
import greencity.enums.SubscriptionType;
import greencity.service.SubscriptionService;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class SubscriptionControllerTest {

    private static final String subscriptionLink = "/subscriptions";

    private MockMvc mockMvc;

    @InjectMocks
    private SubscriptionController subscriptionController;

    @Mock
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(subscriptionController)
            .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
            .build();
    }

    @Test
    void subscribe() throws Exception {
        SubscriptionRequestDto subscriptionRequestDto = new SubscriptionRequestDto();
        subscriptionRequestDto.setEmail("test@example.com");
        subscriptionRequestDto.setSubscriptionType(SubscriptionType.ECO_NEWS);

        SubscriptionResponseDto subscriptionResponseDto = new SubscriptionResponseDto();
        subscriptionResponseDto.setUnsubscribeToken(UUID.randomUUID());

        when(subscriptionService.createSubscription(subscriptionRequestDto)).thenReturn(subscriptionResponseDto);

        mockMvc.perform(post(subscriptionLink)
            .content("""
                {
                  "email": "test@example.com",
                  "subscriptionType": "ECO_NEWS"
                }
                """)
            .contentType(MediaType.APPLICATION_JSON)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isCreated());

        verify(subscriptionService, times(1))
            .createSubscription(any(SubscriptionRequestDto.class));
    }

    @Test
    void unsubscribe() throws Exception {
        UUID unsubscribeToken = UUID.randomUUID();

        mockMvc.perform(delete(subscriptionLink + "/{unsubscribeToken}", unsubscribeToken)
            .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());

        verify(subscriptionService).deleteSubscription(unsubscribeToken);
    }
}