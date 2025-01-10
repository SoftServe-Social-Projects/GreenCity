package greencity.mapping;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.modelmapper.convention.MatchingStrategies;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UtilsMapper {
    private static final ModelMapper modelMapper;

    static {
        modelMapper = new ModelMapper();
        modelMapper.getConfiguration()
            .setMatchingStrategy(MatchingStrategies.STRICT)
            .setFieldMatchingEnabled(true)
            .setSkipNullEnabled(true)
            .setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);
    }

    /**
     * Maps object to another object.
     *
     * @return a mapped object with type {@code D}
     */
    public static <D, T> D map(final T entity, Class<D> outClass) {
        return modelMapper.map(entity, outClass);
    }

    /**
     * Maps list objects to another object and build list this objects.
     *
     * @return list mapped objects with type {@code D}
     */
    public static <D, T> List<D> mapAllToList(final Collection<T> entityList, Class<D> outCLass) {
        return entityList.stream()
            .map(entity -> map(entity, outCLass))
            .collect(Collectors.toList());
    }

    /**
     * Maps list objects to another object and build "Set" this objects.
     *
     * @return set mapped objects with type {@code D}
     */
    public static <D, T> Set<D> mapAllToSet(final Collection<T> entityList, Class<D> outCLass) {
        return entityList.stream()
            .map(entity -> map(entity, outCLass))
            .collect(Collectors.toSet());
    }
}
