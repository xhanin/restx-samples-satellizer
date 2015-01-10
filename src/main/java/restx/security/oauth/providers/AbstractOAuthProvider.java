package restx.security.oauth.providers;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.kevinsawicki.http.HttpRequest;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableMap.Builder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.WebException;
import restx.http.HttpStatus;
import restx.security.oauth.ClientSecretsSettings;
import restx.security.oauth.OAuthPayload;
import restx.security.oauth.OAuthService;

import java.io.IOException;
import java.util.Map;

/**
 * Date: 10/1/15
 * Time: 18:41
 */
public class AbstractOAuthProvider {
    private static final Logger logger = LoggerFactory.getLogger(AbstractOAuthProvider.class);

    public static final String
            CLIENT_ID_KEY = "client_id",
            REDIRECT_URI_KEY = "redirect_uri",
            CLIENT_SECRET = "client_secret", CODE_KEY = "code",
            GRANT_TYPE_KEY = "grant_type",
            AUTH_CODE = "authorization_code";

    protected final ClientSecretsSettings secrets;
    protected final OAuthService OAuthService;

    private final ObjectMapper mapper = new ObjectMapper();
    private final String accessTokenUrl;

    public AbstractOAuthProvider(String accessTokenUrl, OAuthService OAuthService, ClientSecretsSettings secrets) {
        this.accessTokenUrl = accessTokenUrl;
        this.OAuthService = OAuthService;
        this.secrets = secrets;
    }

    protected Map<String, Object> getJsonResponseAsMap(HttpRequest accessTokenRequest) throws IOException {
        return mapper.readValue(accessTokenRequest.body(), new TypeReference<Map<String, Object>>() {});
    }

    protected HttpRequest checkRequest(HttpRequest request) {
        if (request.code() >= 400) {
            String body = request.body();

            logger.info("error processing request {}: {} - {}", request, request.code(), body);

            throw new WebException(HttpStatus.havingCode(request.code()), body);
        }
        return request;
    }

    protected String bearer(String accessToken) {
        return String.format("Bearer %s", accessToken);
    }

    protected Builder<Object, Object> getAccessTokenRequestParams(OAuthPayload payload, String secret) {
        return ImmutableMap.builder()
                .put(CLIENT_ID_KEY, payload.getClientId())
                .put(REDIRECT_URI_KEY, payload.getRedirectUri())
                .put(CLIENT_SECRET, secret)
                .put(CODE_KEY, payload.getCode());
    }

    protected HttpRequest sendAccessTokenRequest(OAuthPayload payload, String secret) {
        HttpRequest accessTokenRequest = HttpRequest.post(accessTokenUrl)
                .form(getAccessTokenRequestParams(payload, secret).build());
        checkRequest(accessTokenRequest);
        return accessTokenRequest;
    }
}
