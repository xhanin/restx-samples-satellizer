package samples.satellizer;

import restx.factory.Component;
import restx.jongo.JongoCollection;
import restx.security.CredentialsStrategy;

import javax.inject.Named;
import java.util.Map;

/**
 * Date: 4/1/15
 * Time: 22:58
 */
@Component
public class AppUserRepository extends JongoOAuthUserRepository<AppUser> {
    public AppUserRepository(@Named("users") JongoCollection users,
                             @Named("usersCredentials") JongoCollection usersCredentials,
                             @Named("usersProviders") JongoCollection usersProviders,
                             CredentialsStrategy credentialsStrategy) {
        super(users, usersCredentials, usersProviders,
                new RefUserByKeyStrategy<AppUser>() {
                    @Override
                    protected String getId(AppUser user) {
                        return user.getId();
                    }
                }, credentialsStrategy,
                AppUser.class, null);
    }

    @Override
    public boolean isAdminDefined() {
        // we don't define a default admin, so we never assume there is no admin defined
        // we need to setup one in DB
        return true;
    }

    @Override
    protected AppUser createNewUserFromProvider(String providerName, String userName, Map<String, Object> userInfo) {
        return createUser(new AppUser().setUserName(userName));
    }
}
