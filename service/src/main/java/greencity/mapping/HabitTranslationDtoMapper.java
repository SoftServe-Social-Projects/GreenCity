package greencity.mapping;

import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.entity.HabitTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HabitTranslationDtoMapper extends AbstractConverter<HabitTranslation, HabitTranslationDto> {
    @Override
    protected HabitTranslationDto convert(HabitTranslation habitTranslation) {
        return HabitTranslationDto.builder()
            .description(habitTranslation.getDescription())
            .habitItem(habitTranslation.getHabitItem())
            .name(habitTranslation.getName())
            .languageCode(habitTranslation.getLanguage().getCode())
            .build();
    }

    /**
     * Method that build {@link List} of {@link HabitTranslationDto} from
     * {@link List} of {@link HabitTranslation}.
     *
     * @param habitTranslationList {@link List} of {@link HabitTranslation}
     * @return {@link List} of {@link HabitTranslationDto}
     * @author Lilia Mokhnatska
     */
    public List<HabitTranslationDto> mapAllToList(List<HabitTranslation> habitTranslationList) {
        return habitTranslationList.stream().map(this::convert).collect(Collectors.toList());
    }
}
