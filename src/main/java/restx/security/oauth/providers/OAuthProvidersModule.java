package restx.security.oauth.providers;

import restx.factory.Module;
import restx.factory.Provides;

import javax.inject.Named;

/**
 * Date: 11/1/15
 * Time: 10:23
 */
@Module(priority = 100)
public class OAuthProvidersModule {
    @Provides
    @Named("restx.twitter.callbackUrl")
    public String twitterCallbackUrl(@Named("restx.server.baseUrl") String baseUrl) {
        // by default the twitter callback url is the server baseUrl
        return baseUrl;
    }

}
