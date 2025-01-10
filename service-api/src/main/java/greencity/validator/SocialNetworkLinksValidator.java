package greencity.validator;

import greencity.annotations.ValidSocialNetworkLinks;
import greencity.constant.ErrorMessage;
import greencity.constant.ServiceValidationConstants;
import greencity.exception.exceptions.BadSocialNetworkLinksException;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SocialNetworkLinksValidator implements ConstraintValidator<ValidSocialNetworkLinks, List<String>> {
    @Override
    public boolean isValid(List<String> links, ConstraintValidatorContext context) {
        if (links.size() > ServiceValidationConstants.MAX_AMOUNT_OF_SOCIAL_NETWORK_LINKS) {
            throw new BadSocialNetworkLinksException(ErrorMessage.USER_CANNOT_ADD_MORE_THAN_5_SOCIAL_NETWORK_LINKS);
        }
        if (!areDistinct(links)) {
            throw new BadSocialNetworkLinksException(ErrorMessage.USER_CANNOT_ADD_SAME_SOCIAL_NETWORK_LINKS);
        }
        links.forEach(UrlValidator::isUrlValid);
        return true;
    }

    private boolean areDistinct(List<String> list) {
        Set<String> hashSet = new HashSet<>(list);
        return (hashSet.size() == list.size());
    }
}
