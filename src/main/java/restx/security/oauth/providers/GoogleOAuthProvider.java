package restx.security.oauth.providers;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap.Builder;
import restx.RestxRequest;
import restx.annotations.POST;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;
import restx.security.oauth.ClientSecretsSettings;
import restx.security.oauth.OAuthPayload;
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
public class GoogleOAuthProvider extends AbstractOAuthProvider {
    public static final String ID = "google";

    private static final String
            ACCESS_TOKEN_URL = "https://accounts.google.com/o/oauth2/token",
            PEOPLE_API_URL = "https://www.googleapis.com/plus/v1/people/me/openIdConnect";

    public GoogleOAuthProvider(ClientSecretsSettings secrets, restx.security.oauth.OAuthService OAuthService) {
        super(ACCESS_TOKEN_URL, OAuthService, secrets);
    }

    @PermitAll
    @POST("/auth/google")
    public Token authenticate(OAuthPayload payload,
                              @Param(kind = Param.Kind.CONTEXT, value = "request") RestxRequest request) throws IOException {
        String accessToken = getAccessToken(payload);

        HttpRequest peopleRequest = HttpRequest.get(PEOPLE_API_URL)
                .authorization(bearer(accessToken));

        checkRequest(peopleRequest);

        Map<String, Object> userInfo = getJsonResponseAsMap(peopleRequest);

        return OAuthService.processUser(request, new ProviderUserInfo()
                .setProviderName(ID)
                .setUserIdForProvider((String) userInfo.get("sub"))
                .setDisplayName(Optional.of((String) userInfo.get("name")))
                .setEmail(Optional.fromNullable((String) userInfo.get("email")))
                .setUserInfo(userInfo));
    }

    protected String getAccessToken(OAuthPayload payload) throws IOException {
        HttpRequest accessTokenRequest = sendAccessTokenRequest(payload, secrets.getGoogle().get());
        return (String) getJsonResponseAsMap(accessTokenRequest).get("access_token");
    }

    @Override
    protected Builder<Object, Object> getAccessTokenRequestParams(OAuthPayload payload, String secret) {
        return super.getAccessTokenRequestParams(payload, secret).put(GRANT_TYPE_KEY, AUTH_CODE);
    }
}
