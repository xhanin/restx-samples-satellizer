package samples.satellizer;

import org.bson.types.ObjectId;
import restx.factory.Component;
import restx.jongo.JongoCollection;
import restx.security.CredentialsStrategy;

import javax.inject.Named;

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
                new RefUserByNameStrategy<AppUser>() {
                    @Override
                    public String getNameProperty() {
                        return "_id";
                    }
                }, credentialsStrategy,
                AppUser.class,
                // get NPE if default admin is not defined
                new AppUser().setId(new ObjectId().toString()));
    }

    @Override
    public boolean isAdminDefined() {
        // we don't define a default admin, so we never assume there is no admin defined
        // we need to setup one in DB
        return true;
    }

    @Override
    protected AppUser createNewUserFromProvider(ProviderUserInfo providerUserInfo) {
        return createUser(new AppUser()
                .setDisplayName(providerUserInfo.getDisplayName().or(""))
                .setEmail(providerUserInfo.getEmail().or(""))
        );
    }
}
