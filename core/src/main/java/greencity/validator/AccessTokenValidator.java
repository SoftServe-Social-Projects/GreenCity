package greencity.validator;

import greencity.annotations.ValidAccessToken;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AccessTokenValidator implements ConstraintValidator<ValidAccessToken, String> {
    @Override
    public boolean isValid(String accessToken, ConstraintValidatorContext constraintValidatorContext) {
        return accessToken != null && accessToken.matches("^[a-zA-Z0-9-._~+]+$");
    }
}
