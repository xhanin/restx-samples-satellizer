package samples.satellizer;

import com.google.common.base.Optional;
import restx.RestxRequest;
import restx.WebException;
import restx.annotations.POST;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.security.CredentialsStrategy;
import restx.security.PermitAll;


/**
 * Date: 5/1/15
 * Time: 22:49
 */
@Component
@RestxResource
public class LoginResource {
    private final AppUserRepository userRepository;
    private final AuthUtils authUtils;
    private final CredentialsStrategy credentialsStrategy;

    public LoginResource(AppUserRepository userRepository, AuthUtils authUtils, CredentialsStrategy credentialsStrategy) {
        this.userRepository = userRepository;
        this.authUtils = authUtils;
        this.credentialsStrategy = credentialsStrategy;
    }

    @PermitAll
    @POST("/auth/login")
    public Token login(AppUserLogin login,
                        @Param(kind = Param.Kind.CONTEXT, value = "request") RestxRequest request) {
        Optional<AppUser> user = userRepository.findByEmail(login.getEmail());

        if (!user.isPresent()) {
            throw new WebException(HttpStatus.UNAUTHORIZED, "Invalid login, check email and password");
        }

        Optional<String> credentials = userRepository.findCredentialByUserName(user.get().getName());
        if (!credentials.isPresent()) {
            throw new WebException(HttpStatus.UNAUTHORIZED, "Invalid login, check email and password");
        }

        if (!credentialsStrategy.checkCredentials(user.get().getName(), login.getPassword(), credentials.get())) {
            throw new WebException(HttpStatus.UNAUTHORIZED, "Invalid login, check email and password");
        }

        return authUtils.createToken(request.getClientAddress(), user.get().getName());
    }
}
