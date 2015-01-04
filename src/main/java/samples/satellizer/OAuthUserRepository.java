package samples.satellizer;

import com.google.common.base.Optional;
import restx.security.RestxPrincipal;
import restx.security.UserRepository;

import java.util.Map;

/**
 * Date: 4/1/15
 * Time: 22:20
 */
public interface OAuthUserRepository<U extends RestxPrincipal> extends UserRepository<U> {
    Optional<U> findByProvider(String providerName, String userIdForProvider);
    void linkProviderAccount(U user, String providerName, String userIdForProvider, String userName, Map<String, Object> userInfo);
    U createNewUserWithLinkedProviderAccount(String providerName, String userIdForProvider, String userName, Map<String, Object> userInfo);
}
