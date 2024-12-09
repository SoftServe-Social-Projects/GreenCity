package greencity.mapping;

import greencity.dto.habittranslation.HabitTranslationDto;
import greencity.entity.HabitTranslation;
import org.modelmapper.AbstractConverter;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class HabitTranslationMapper extends AbstractConverter<HabitTranslationDto, HabitTranslation> {
    @Override
    protected HabitTranslation convert(HabitTranslationDto habitTranslationDto) {
        return HabitTranslation.builder()
            .description(habitTranslationDto.getDescription())
            .habitItem(habitTranslationDto.getHabitItem())
            .name(habitTranslationDto.getName())
            .build();
    }

    /**
     * Additional method that build {@link HabitTranslation} from
     * {@link HabitTranslationDto} but from nameUa, descriptionUa, habitItemUa
     * fields if they not null.
     *
     * @param habitTranslationDto {@link HabitTranslationDto}
     * @return {@link HabitTranslation}
     *
     * @author Chernenko Vitaliy
     */
    public HabitTranslation convertUa(HabitTranslationDto habitTranslationDto) {
        HabitTranslation habitTranslation = new HabitTranslation();
        if (habitTranslationDto.getNameUa() == null) {
            habitTranslation.setName(habitTranslationDto.getName());
        } else {
            habitTranslation.setName(habitTranslationDto.getNameUa());
        }
        if (habitTranslationDto.getDescriptionUa() == null) {
            habitTranslation.setDescription(habitTranslationDto.getDescription());
        } else {
            habitTranslation.setDescription(habitTranslationDto.getDescriptionUa());
        }
        if (habitTranslationDto.getHabitItemUa() == null) {
            habitTranslation.setHabitItem(habitTranslationDto.getHabitItem());
        } else {
            habitTranslation.setHabitItem(habitTranslationDto.getHabitItemUa());
        }

        return habitTranslation;
    }

    /**
     * Method that build {@link List} of {@link HabitTranslation} from {@link List}
     * of {@link HabitTranslationDto}.
     *
     * @param dtoList {@link List} of {@link HabitTranslationDto}
     * @return {@link List} of {@link HabitTranslation}
     * @author Lilia Mokhnatska
     */
    public List<HabitTranslation> mapAllToList(List<HabitTranslationDto> dtoList) {
        return dtoList.stream().map(this::convert).collect(Collectors.toList());
    }

    /**
     * Method that build {@link List} of {@link HabitTranslation} from {@link List}
     * of {@link HabitTranslationDto} and {@link String} language.
     *
     * @param dtoList  {@link List} of {@link HabitTranslationDto}
     * @param language {@link String}
     *
     * @return {@link List} of {@link HabitTranslation}
     *
     * @author Chernenko Vitaliy
     */
    public List<HabitTranslation> mapAllToList(List<HabitTranslationDto> dtoList, String language) {
        if (language.equals("ua")) {
            return dtoList.stream().map(this::convertUa).collect(Collectors.toList());
        }
        return dtoList.stream().map(this::convert).collect(Collectors.toList());
    }
}
