package samples.satellizer.rest;

import restx.annotations.GET;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.security.RestxSession;
import restx.security.oauth.OAuthUserRepository;
import samples.satellizer.SatellizerException;

/**
 * Date: 10/1/15
 * Time: 17:49
 */
@Component
@RestxResource
public class UnlinkProviderResource {
    private final OAuthUserRepository userRepository;

    public UnlinkProviderResource(OAuthUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GET("/auth/unlink/:providerName")
    public void unlink(String providerName) {
        try {
            userRepository.unlinkProviderAccount(RestxSession.current().getPrincipal().get(), providerName);
        } catch (IllegalStateException e) {
            throw new SatellizerException(HttpStatus.UNAUTHORIZED, e.getMessage());
        }
    }
}
