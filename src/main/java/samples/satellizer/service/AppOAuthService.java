package samples.satellizer.service;

import restx.factory.Component;
import restx.security.oauth.ClientSecretsSettings;
import restx.security.oauth.OAuthService;
import samples.satellizer.domain.AppUser;

/**
 * Date: 4/1/15
 * Time: 23:02
 */
@Component
public class AppOAuthService extends OAuthService<AppUser> {
    public AppOAuthService(ClientSecretsSettings secrets, AppUserRepository dao) {
        super(secrets, dao);
    }
}
