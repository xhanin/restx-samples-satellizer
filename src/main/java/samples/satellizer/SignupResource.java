package samples.satellizer;

import restx.RestxRequest;
import restx.annotations.POST;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.security.PermitAll;

/**
 * Date: 5/1/15
 * Time: 22:17
 */
@Component
@RestxResource
public class SignupResource {
    private final AppUserRepository userRepository;
    private final OAuthService OAuthService;

    public SignupResource(AppUserRepository userRepository, OAuthService OAuthService) {
        this.userRepository = userRepository;
        this.OAuthService = OAuthService;
    }

    @PermitAll
    @POST("/auth/signup")
    public Token signup(AppUserSignup signup,
                        @Param(kind = Param.Kind.CONTEXT, value = "request") RestxRequest request) {
        if (userRepository.findByEmail(signup.getEmail()).isPresent()) {
            throw new SatellizerException(HttpStatus.UNAUTHORIZED, "a user with that email is already registered");
        }

        AppUser u = userRepository.createUser(
                userRepository.newAppUser()
                        .setEmail(signup.getEmail()).setDisplayName(signup.getDisplayName()));

        signup.setId(u.getId());

        userRepository.setCredentials(u.getName(), signup.getPassword());

        return OAuthService.createToken(request.getClientAddress(), u.getName());
    }
}
