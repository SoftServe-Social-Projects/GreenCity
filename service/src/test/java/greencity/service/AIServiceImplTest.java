package greencity.service;

import greencity.constant.OpenAIRequest;
import greencity.dto.habit.DurationHabitDto;
import greencity.entity.HabitAssign;
import greencity.enums.HabitAssignStatus;
import greencity.repository.HabitAssignRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.util.List;
import static greencity.ModelUtils.getHabitAssign;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AIServiceImplTest {
    @Mock
    private OpenAIService openAIService;
    @Mock
    private HabitAssignRepo habitAssignRepo;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private AIServiceImpl aiServiceImpl;
    private HabitAssign habitAssign = getHabitAssign(HabitAssignStatus.INPROGRESS);
    private DurationHabitDto durationHabitDto = new DurationHabitDto("", 0L);
    private Long userId = 1L;
    private String language = "en";

    @Test
    void getForecast_ReturnsResponse_FromOpenAIService() {
        when(habitAssignRepo.findAllByUserId(userId)).thenReturn(List.of(habitAssign));
        when(modelMapper.map(habitAssign, DurationHabitDto.class)).thenReturn(durationHabitDto);
        when(openAIService.makeRequest("en" + OpenAIRequest.FORECAST + List.of(durationHabitDto)))
                .thenReturn("Forecast Response");

        String result = aiServiceImpl.getForecast(userId, language);

        assertThat(result).isEqualTo("Forecast Response");
        verify(habitAssignRepo).findAllByUserId(userId);
        verify(modelMapper).map(habitAssign, DurationHabitDto.class);
        verify(openAIService).makeRequest("en" + OpenAIRequest.FORECAST + List.of(durationHabitDto));
    }

    @Test
    void getForecast_ThrowsException_WhenOpenAIServiceFails() {
        when(habitAssignRepo.findAllByUserId(userId)).thenReturn(List.of(habitAssign));
        when(modelMapper.map(habitAssign, DurationHabitDto.class)).thenReturn(durationHabitDto);
        when(openAIService.makeRequest("en" + OpenAIRequest.FORECAST + List.of(durationHabitDto)))
                .thenThrow(new RuntimeException("OpenAI Service Failed"));

        assertThrows(RuntimeException.class, () -> aiServiceImpl.getForecast(userId, language));

        verify(habitAssignRepo).findAllByUserId(userId);
        verify(modelMapper).map(habitAssign, DurationHabitDto.class);
        verify(openAIService).makeRequest("en" + OpenAIRequest.FORECAST + List.of(durationHabitDto));
    }
}