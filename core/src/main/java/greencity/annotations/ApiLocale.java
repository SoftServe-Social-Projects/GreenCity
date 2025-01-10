package greencity.annotations;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used for choosing language code as a query parameter for
 * example: "en", "ua".
 *
 * @author Yurii Savchenko
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Parameter(name = "lang", schema = @Schema(type = "string"), in = ParameterIn.QUERY,
    description = "Code of the needed language.")
public @interface ApiLocale {
}
