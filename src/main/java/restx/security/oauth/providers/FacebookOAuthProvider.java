package restx.security.oauth.providers;

import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.base.Optional;
import restx.RestxRequest;
import restx.annotations.POST;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.PermitAll;
import restx.security.oauth.ClientSecretsSettings;
import restx.security.oauth.OAuthPayload;
import restx.security.oauth.OAuthUserService;
import restx.security.oauth.ProviderUserInfo;
import restx.security.oauth.Token;

import java.io.IOException;
import java.util.Map;

/**
 * Date: 10/1/15
 * Time: 18:40
 */
@Component
@RestxResource(group = "auth")
public class FacebookOAuthProvider extends AbstractOAuthProvider {
    public static final String ID = "facebook";

    private static final String ACCESS_TOKEN_URL = "https://graph.facebook.com/oauth/access_token",
        GRAPH_API_URL = "https://graph.facebook.com/me";

    public FacebookOAuthProvider(OAuthUserService OAuthUserService, ClientSecretsSettings secrets) {
        super(ACCESS_TOKEN_URL, OAuthUserService, secrets);
    }

    @PermitAll
    @POST("/auth/facebook")
    public Token authenticate(OAuthPayload payload,
                              @Param(kind = Param.Kind.CONTEXT, value = "request") RestxRequest request) throws IOException {
        HttpRequest accessTokenRequest = sendAccessTokenRequest(payload, secrets.getFacebook().get());

        HttpRequest graphRequest = checkRequest(HttpRequest.get(GRAPH_API_URL + "?" + accessTokenRequest.body()));

        Map<String, Object> userInfo = getJsonResponseAsMap(graphRequest);

        return OAuthUserService.processUser(request, new ProviderUserInfo()
                .setProviderName(ID)
                .setUserIdForProvider((String) userInfo.get("id"))
                .setDisplayName(Optional.of((String) userInfo.get("name")))
                .setEmail(Optional.fromNullable((String) userInfo.get("email")))
                .setUserInfo(userInfo));
    }
}
