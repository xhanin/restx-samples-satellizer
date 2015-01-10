package samples.satellizer;

import com.google.common.base.Optional;
import restx.security.RestxPrincipal;
import restx.security.UserRepository;

/**
 * Date: 4/1/15
 * Time: 22:20
 */
public interface OAuthUserRepository<U extends RestxPrincipal> extends UserRepository<U> {
    boolean hasProvider(U user, String providerName);
    Optional<U> findByProvider(String providerName, String userIdForProvider);

    void linkProviderAccount(U user, ProviderUserInfo providerUserInfo);
    void unlinkProviderAccount(U user, String providerName);
    U createNewUserWithLinkedProviderAccount(ProviderUserInfo providerUserInfo);
}
