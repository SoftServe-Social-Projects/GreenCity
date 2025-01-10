package greencity.converters;

import java.time.ZonedDateTime;

/**
 * Service that converts dates to ZonedDateTime that corresponds to datasource
 * timezone.
 *
 * @author Yurii Koval
 */
public interface DateService {
    /**
     * Returns converted {@link ZonedDateTime} to datasource timezone.
     *
     * @param toConvert date that should be converted.
     * @return converted to datasource timezone {@link ZonedDateTime}.
     */
    ZonedDateTime convertToDatasourceTimezone(ZonedDateTime toConvert);
}
