package greencity.service;

import greencity.constant.OpenAIRequest;
import greencity.dto.habit.DurationHabitDto;
import greencity.dto.habit.ShortHabitDto;
import greencity.entity.Habit;
import greencity.entity.HabitAssign;
import greencity.entity.HabitTranslation;
import greencity.enums.HabitAssignStatus;
import greencity.repository.HabitAssignRepo;
import greencity.repository.HabitRepo;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import java.util.Collections;
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
    private HabitRepo habitRepo;
    @Mock
    private ModelMapper modelMapper;
    @InjectMocks
    private AIServiceImpl aiServiceImpl;
    private HabitAssign habitAssign = getHabitAssign(HabitAssignStatus.INPROGRESS);
    private DurationHabitDto durationHabitDto = new DurationHabitDto("", 0L);
    private Long id = 1L;
    private String language = "en";
    private final String habitTranslationName = "Test Habit";
    private final HabitTranslation habitTranslation = HabitTranslation.builder().name(habitTranslationName).build();

    private final Habit habit = Habit.builder()
        .id(id)
        .habitTranslations(Collections.singletonList(habitTranslation))
        .build();
    private final ShortHabitDto shortHabitDto = new ShortHabitDto(id, habitTranslationName);

    @Test
    void getForecast_ReturnsResponse_FromOpenAIService() {
        when(habitAssignRepo.findAllByUserId(id)).thenReturn(List.of(habitAssign));
        when(modelMapper.map(habitAssign, DurationHabitDto.class)).thenReturn(durationHabitDto);
        when(openAIService.makeRequest("en" + OpenAIRequest.FORECAST + List.of(durationHabitDto)))
                .thenReturn("Forecast Response");

        String result = aiServiceImpl.getForecast(id, language);

        assertThat(result).isEqualTo("Forecast Response");
        verify(habitAssignRepo).findAllByUserId(id);
        verify(modelMapper).map(habitAssign, DurationHabitDto.class);
        verify(openAIService).makeRequest("en" + OpenAIRequest.FORECAST + List.of(durationHabitDto));
    }

    @Test
    void getForecast_ThrowsException_WhenOpenAIServiceFails() {
        when(habitAssignRepo.findAllByUserId(id)).thenReturn(List.of(habitAssign));
        when(modelMapper.map(habitAssign, DurationHabitDto.class)).thenReturn(durationHabitDto);
        when(openAIService.makeRequest("en" + OpenAIRequest.FORECAST + List.of(durationHabitDto)))
                .thenThrow(new RuntimeException("OpenAI Service Failed"));

        assertThrows(RuntimeException.class, () -> aiServiceImpl.getForecast(id, language));

        verify(habitAssignRepo).findAllByUserId(id);
        verify(modelMapper).map(habitAssign, DurationHabitDto.class);
        verify(openAIService).makeRequest("en" + OpenAIRequest.FORECAST + List.of(durationHabitDto));
    }

    @Test
    void getForecast_CallsGetAdvice_WhenHabitAssignsIsEmpty() {
        when(habitAssignRepo.findAllByUserId(id)).thenReturn(Collections.emptyList());
        when(habitRepo.findRandomHabit()).thenReturn(habit);
        when(modelMapper.map(habit, ShortHabitDto.class)).thenReturn(shortHabitDto);
        when(openAIService.makeRequest("en" + OpenAIRequest.ADVICE + shortHabitDto))
                .thenReturn("Advice Response");

        String result = aiServiceImpl.getForecast(id, language);

        assertThat(result).isEqualTo("Advice Response");
        verify(habitAssignRepo).findAllByUserId(id);
        verify(habitRepo).findRandomHabit();
        verify(modelMapper).map(habit, ShortHabitDto.class);
        verify(openAIService).makeRequest("en" + OpenAIRequest.ADVICE + shortHabitDto);
    }

    @Test
    void getAdvice_ReturnsResponse_FromOpenAIService() {
        when(habitRepo.findRandomHabit()).thenReturn(habit);
        when(modelMapper.map(habit, ShortHabitDto.class)).thenReturn(shortHabitDto);
        when(openAIService.makeRequest("en" + OpenAIRequest.ADVICE + shortHabitDto))
                .thenReturn("Advice Response");

        String result = aiServiceImpl.getAdvice(id, language);

        assertThat(result).isEqualTo("Advice Response");
        verify(habitRepo).findRandomHabit();
        verify(modelMapper).map(habit, ShortHabitDto.class);
        verify(openAIService).makeRequest("en" + OpenAIRequest.ADVICE + shortHabitDto);
    }

    @Test
    void getAdvice_ThrowsException_WhenOpenAIServiceFails() {
        when(habitRepo.findRandomHabit()).thenReturn(habit);
        when(modelMapper.map(habit, ShortHabitDto.class)).thenReturn(shortHabitDto);
        when(openAIService.makeRequest("en" + OpenAIRequest.ADVICE + shortHabitDto))
                .thenThrow(new RuntimeException("OpenAI Service Failed"));

        assertThrows(RuntimeException.class, () -> aiServiceImpl.getAdvice(id, language));

        verify(habitRepo).findRandomHabit();
        verify(modelMapper).map(habit, ShortHabitDto.class);
        verify(openAIService).makeRequest("en" + OpenAIRequest.ADVICE + shortHabitDto);
    }
}