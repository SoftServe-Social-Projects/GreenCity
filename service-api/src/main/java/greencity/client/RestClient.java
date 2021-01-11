package greencity.client;

import greencity.constant.RestTemplateLinks;
import greencity.dto.user.UserVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
@Component
public class RestClient {
    private final RestTemplate restTemplate;
    @Value("${greencityuser.server.address}")
    private final String greenCityUserServerAddress;

    /**
     * Method find user by principal.
     *
     * @param email of {@link UserVO}
     * @author Orest Mamchuk
     */
    public UserVO findByEmail(String email) {
        HttpHeaders headers = new HttpHeaders();
        String url = greenCityUserServerAddress
            + RestTemplateLinks.USER_FIND_BY_EMAIL + RestTemplateLinks.EMAIL + email;
        return restTemplate.exchange(url, HttpMethod.GET,
            new HttpEntity<>(headers), UserVO.class).getBody();
    }
}
