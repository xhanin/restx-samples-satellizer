package samples.satellizer;

import com.google.common.base.Optional;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.ObjectId;
import restx.jongo.JongoCollection;
import restx.jongo.JongoUserRepository;
import restx.security.CredentialsStrategy;
import restx.security.RestxPrincipal;

/**
 * Date: 4/1/15
 * Time: 22:29
 */
public abstract class JongoOAuthUserRepository<U extends RestxPrincipal> extends JongoUserRepository<U> implements OAuthUserRepository<U> {

    public static class ProviderUser {
        @Id @ObjectId
        private String id;

        private String provider;
        private String providerUserId;
        private String userRef;

        public String getId() {
            return id;
        }

        public String getProvider() {
            return provider;
        }

        public String getProviderUserId() {
            return providerUserId;
        }

        public String getUserRef() {
            return userRef;
        }

        public ProviderUser setId(final String id) {
            this.id = id;
            return this;
        }

        public ProviderUser setProvider(final String provider) {
            this.provider = provider;
            return this;
        }

        public ProviderUser setProviderUserId(final String providerUserId) {
            this.providerUserId = providerUserId;
            return this;
        }

        public ProviderUser setUserRef(final String userRef) {
            this.userRef = userRef;
            return this;
        }

        @Override
        public String toString() {
            return "ProviderUser{" +
                    "id='" + id + '\'' +
                    ", provider='" + provider + '\'' +
                    ", providerUserId='" + providerUserId + '\'' +
                    ", userRef='" + userRef + '\'' +
                    '}';
        }
    }

    private final JongoCollection usersProviders;
    private final UserRefStrategy<U> userRefStrategy;

    public JongoOAuthUserRepository(JongoCollection users,
                                    JongoCollection usersCredentials,
                                    JongoCollection usersProviders,
                                    UserRefStrategy<U> userRefStrategy,
                                    CredentialsStrategy credentialsStrategy,
                                    Class<U> userClass,
                                    U defaultAdminUser) {
        super(users, usersCredentials, userRefStrategy, credentialsStrategy, userClass, defaultAdminUser);
        this.usersProviders = usersProviders;
        this.userRefStrategy = userRefStrategy;
    }

    @Override
    public boolean hasProvider(U user, String providerName) {
        return usersProviders.get()
                .findOne("{provider: #, userRef: #}", providerName, userRefStrategy.getUserRef(user))
                .as(ProviderUser.class) != null;
    }

    @Override
    public Optional<U> findByProvider(String providerName, String userIdForProvider) {
        ProviderUser providerUser = usersProviders.get()
                .findOne("{provider: #, providerUserId: #}", providerName, userIdForProvider).as(ProviderUser.class);

        if (providerUser == null) {
            return Optional.absent();
        }

        return findUserByKey(providerUser.userRef);
    }

    @Override
    public void linkProviderAccount(U user, ProviderUserInfo providerUserInfo) {
        usersProviders.get().save(new ProviderUser()
                .setProvider(providerUserInfo.getProviderName())
                .setProviderUserId(providerUserInfo.getUserIdForProvider())
                .setUserRef(userRefStrategy.getUserRef(user))
                );
    }

    @Override
    public U createNewUserWithLinkedProviderAccount(ProviderUserInfo providerUserInfo) {
        U user = createNewUserFromProvider(providerUserInfo);

        linkProviderAccount(user, providerUserInfo);

        return user;
    }

    protected abstract U createNewUserFromProvider(ProviderUserInfo providerUserInfo);
}
