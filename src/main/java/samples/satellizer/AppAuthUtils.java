package samples.satellizer;

import restx.factory.Component;

/**
 * Date: 4/1/15
 * Time: 23:02
 */
@Component
public class AppAuthUtils extends AuthUtils<AppUser> {
    public AppAuthUtils(ClientSecretsSettings secrets, AppUserRepository dao) {
        super(secrets, dao);
    }
}
