package samples.satellizer.service;

import com.google.common.base.Optional;
import org.bson.types.ObjectId;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.jongo.JongoCollection;
import restx.security.CredentialsStrategy;
import restx.security.oauth.ProviderUserInfo;
import restx.security.oauth.jongo.JongoOAuthUserRepository;
import samples.satellizer.SatellizerException;
import samples.satellizer.domain.AppUser;

import javax.inject.Named;

/**
 * Date: 4/1/15
 * Time: 22:58
 */
@Component
public class AppUserRepository extends JongoOAuthUserRepository<AppUser> {
    private final JongoCollection users;

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
        this.users = users;
    }

    @Override
    public boolean isAdminDefined() {
        // we don't define a default admin, so we never assume there is no admin defined
        // we need to setup one in DB
        return true;
    }

    @Override
    protected AppUser createNewUserFromProvider(ProviderUserInfo providerUserInfo) {
        return createUser(newAppUser()
                        .setDisplayName(providerUserInfo.getDisplayName().or(""))
                        .setEmail(providerUserInfo.getEmail().or(""))
        );
    }

    @Override
    public AppUser createNewUserWithLinkedProviderAccount(ProviderUserInfo providerUserInfo) {
        // prevent registering a user by provider auth with same email as existing one
        if (providerUserInfo.getEmail().isPresent()
                && findByEmail(providerUserInfo.getEmail().get()).isPresent()) {
            throw new SatellizerException(HttpStatus.UNAUTHORIZED,
                    "can't create user for " + providerUserInfo.getEmail().get() + ":" +
                            " a user with that email is already registered. Please login first" +
                            " and then choose 'link' on the profile page");
        }
        return super.createNewUserWithLinkedProviderAccount(providerUserInfo);
    }

    public AppUser newAppUser() {
        return new AppUser().setId("U-" + new ObjectId());
    }

    public Optional<AppUser> findByEmail(String email) {
        return Optional.fromNullable(users.get().findOne("{email: #}", email).as(AppUser.class));
    }
}
