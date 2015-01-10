package samples.satellizer.rest;

import restx.annotations.GET;
import restx.annotations.PUT;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.security.RestxSession;
import samples.satellizer.domain.AppUser;
import samples.satellizer.domain.AppUserProfile;
import samples.satellizer.service.AppUserRepository;

/**
 * Date: 5/1/15
 * Time: 20:57
 */
@Component
@RestxResource
public class MeResource {
    private final AppUserRepository userRepository;

    public MeResource(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GET("/me")
    public AppUserProfile getCurrentUser() {
        AppUser user = (AppUser) RestxSession.current().getPrincipal().get();

        AppUserProfile profile = new AppUserProfile();

        profile.setId(user.getId())
                .setDisplayName(user.getDisplayName())
                .setEmail(user.getEmail());

        loadProviders(user, profile);

        return profile;
    }

    @PUT("/me")
    public AppUserProfile updateCurrentUser(AppUserProfile profile) {
        AppUser user = (AppUser) RestxSession.current().getPrincipal().get();

        userRepository.updateUser(user.setDisplayName(profile.getDisplayName()).setEmail(profile.getEmail()));

        // reload providers data to avoid returning false data
        loadProviders(user, profile);

        return profile;
    }

    private void loadProviders(AppUser user, AppUserProfile profile) {
        profile
                .setGoogle(userRepository.hasProvider(user, "google"))
                .setFacebook(userRepository.hasProvider(user, "facebook"));
    }
}
