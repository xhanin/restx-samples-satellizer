package restx.security.oauth.providers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.RestxRequest;
import restx.WebException;
import restx.annotations.POST;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.security.PermitAll;
import restx.security.oauth.ClientSecretsSettings;
import restx.security.oauth.OAuthPayload;
import restx.security.oauth.OAuthService;
import restx.security.oauth.ProviderUserInfo;
import restx.security.oauth.Token;

import java.io.IOException;
import java.util.Map;

/**
 * Date: 4/1/15
 * Time: 20:47
 */
@Component
@RestxResource(group = "auth")
public class GoogleAuthProvider {
    private static final Logger logger = LoggerFactory.getLogger(GoogleAuthProvider.class);

    public static final String
            CLIENT_ID_KEY = "client_id",
            REDIRECT_URI_KEY = "redirect_uri",
            CLIENT_SECRET = "client_secret", CODE_KEY = "code",
            GRANT_TYPE_KEY = "grant_type",
            AUTH_CODE = "authorization_code";

    public static final String
            ACCESS_TOKEN_URL = "https://accounts.google.com/o/oauth2/token",
            PEOPLE_API_URL = "https://www.googleapis.com/plus/v1/people/me/openIdConnect";

    private final ObjectMapper mapper = new ObjectMapper();

    private final ClientSecretsSettings secrets;
    private final OAuthService OAuthService;

    public GoogleAuthProvider(ClientSecretsSettings secrets, restx.security.oauth.OAuthService OAuthService) {
        this.secrets = secrets;
        this.OAuthService = OAuthService;
    }

    @PermitAll
    @POST("/auth/google")
    public Token authenticate(OAuthPayload payload,
                              @Param(kind = Param.Kind.CONTEXT, value = "request") RestxRequest request) throws IOException {
        String accessToken = getAccessToken(payload);

        HttpRequest peopleRequest = HttpRequest.get(PEOPLE_API_URL).authorization(String.format("Bearer %s", accessToken));

        checkRequest(peopleRequest);

        Map<String, Object> userInfo = getResponseAsMap(peopleRequest);
        String userId = (String) userInfo.get("sub");
        String userName = (String) userInfo.get("name");
        String email = (String) userInfo.get("email");

        logger.debug("{} authenticated with Google - {}", userName, userInfo);

        return OAuthService.processUser(request, new ProviderUserInfo()
                .setProviderName("google")
                .setUserIdForProvider(userId)
                .setDisplayName(Optional.of(userName))
                .setEmail(Optional.fromNullable(email))
                .setUserInfo(userInfo));
    }

    private String getAccessToken(OAuthPayload payload) throws IOException {
        HttpRequest accessTokenRequest = HttpRequest.post(ACCESS_TOKEN_URL)
                .form(ImmutableMap.of(
                        CLIENT_ID_KEY, payload.getClientId(),
                        REDIRECT_URI_KEY, payload.getRedirectUri(),
                        CLIENT_SECRET, secrets.getGoogle().get(),
                        CODE_KEY, payload.getCode(),
                        GRANT_TYPE_KEY, AUTH_CODE
                ));

        checkRequest(accessTokenRequest);

        Map<String, Object> response = getResponseAsMap(accessTokenRequest);

        return (String) response.get("access_token");
    }

    private Map<String, Object> getResponseAsMap(HttpRequest accessTokenRequest) throws IOException {
        return mapper.readValue(accessTokenRequest.body(), new TypeReference<Map<String, Object>>() {});
    }

    private void checkRequest(HttpRequest request) {
        if (request.code() >= 400) {
            String body = request.body();

            logger.info("error processing request {}: {} - {}", request, request.code(), body);

            throw new WebException(HttpStatus.havingCode(request.code()), body);
        }
    }
}
