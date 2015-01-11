package restx.security.oauth.providers;

import com.google.common.base.Optional;
import com.google.common.base.Splitter;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.Verifier;
import restx.RestxRequest;
import restx.RestxResponse;
import restx.WebException;
import restx.annotations.GET;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.security.PermitAll;
import restx.security.oauth.ClientSecretsSettings;
import restx.security.oauth.OAuthUserService;
import restx.security.oauth.ProviderUserInfo;
import restx.security.oauth.Token;

import javax.inject.Named;
import java.io.IOException;
import java.util.Map;

/**
 * Date: 10/1/15
 * Time: 18:40
 */
@Component
@RestxResource(group = "auth")
public class TwitterOAuthProvider {
    public static final String ID = "twitter";

    private final OAuthUserService oAuthUserService;
    private final org.scribe.oauth.OAuthService service;

    public TwitterOAuthProvider(OAuthUserService oAuthUserService, ClientSecretsSettings secrets,
                                @Named("restx.twitter.callbackUrl") String baseUrl) {
        this.oAuthUserService = oAuthUserService;

        service = new ServiceBuilder()
                .provider(TwitterApi.class)
                .apiKey(secrets.getTwitterConsumerKey().get())
                .apiSecret(secrets.getTwitterConsumerSecret().get())
                .callback(baseUrl)
                .build();
    }

    @PermitAll
    @GET("/auth/twitter")
    public Token authenticate(Optional<String> oauth_token, Optional<String> oauth_verifier,
                              @Param(kind = Param.Kind.CONTEXT, value = "request") RestxRequest request) throws IOException {
        /* this endpoint is used for 2 purpose:
        1) to obtain the twitter authenticate URL, to redirect the user to it
        2) to check the user has properly signed in with twitter and performs the OAuth request

        We distinguish the 2 cases based on the presence of oauth_token and oauth_verifier query strings
         */

        if (!oauth_token.isPresent() || !oauth_verifier.isPresent()) {
            org.scribe.model.Token requestToken = service.getRequestToken();
            return redirectTo(service.getAuthorizationUrl(requestToken));
        } else {
            Verifier verifier = new Verifier(oauth_verifier.get());
            org.scribe.model.Token token = new org.scribe.model.Token(oauth_token.get(), "");
            org.scribe.model.Token accessToken = service.getAccessToken(token, verifier);

            Map<String, String> userInfo = Splitter.on('&').withKeyValueSeparator('=').split(accessToken.getRawResponse());

            return oAuthUserService.processUser(request, new ProviderUserInfo()
                    .setProviderName(ID)
                    .setUserIdForProvider(userInfo.get("user_id"))
                    .setDisplayName(Optional.of(userInfo.get("screen_name")))
                    .setEmail(Optional.<String>absent())
                    .setUserInfo(userInfo));
        }
    }

    protected Token redirectTo(final String url) {
        throw new WebException(HttpStatus.FOUND) {
            @Override
            public void writeTo(RestxRequest restxRequest, RestxResponse restxResponse) throws IOException {
                restxResponse
                        .setStatus(getStatus())
                        .setHeader("Location", url);
            }
        };
    }
}
