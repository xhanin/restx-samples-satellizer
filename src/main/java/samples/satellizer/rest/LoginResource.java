package samples.satellizer.rest;

import com.google.common.base.Optional;
import restx.RestxRequest;
import restx.annotations.POST;
import restx.annotations.Param;
import restx.annotations.RestxResource;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.security.CredentialsStrategy;
import restx.security.PermitAll;
import restx.security.oauth.OAuthUserService;
import restx.security.oauth.Token;
import samples.satellizer.domain.AppUser;
import samples.satellizer.domain.AppUserLogin;
import samples.satellizer.service.AppUserRepository;
import samples.satellizer.SatellizerException;


/**
 * Date: 5/1/15
 * Time: 22:49
 */
@Component
@RestxResource
public class LoginResource {
    private final AppUserRepository userRepository;
    private final OAuthUserService OAuthUserService;
    private final CredentialsStrategy credentialsStrategy;

    public LoginResource(AppUserRepository userRepository, OAuthUserService OAuthUserService, CredentialsStrategy credentialsStrategy) {
        this.userRepository = userRepository;
        this.OAuthUserService = OAuthUserService;
        this.credentialsStrategy = credentialsStrategy;
    }

    @PermitAll
    @POST("/auth/login")
    public Token login(AppUserLogin login,
                        @Param(kind = Param.Kind.CONTEXT, value = "request") RestxRequest request) {
        Optional<AppUser> user = userRepository.findByEmail(login.getEmail());

        if (!user.isPresent()) {
            throw new SatellizerException(HttpStatus.UNAUTHORIZED, "Invalid login, check email and password");
        }

        Optional<String> credentials = userRepository.findCredentialByUserName(user.get().getName());
        if (!credentials.isPresent()) {
            throw new SatellizerException(HttpStatus.UNAUTHORIZED, "Invalid login, check email and password");
        }

        if (!credentialsStrategy.checkCredentials(user.get().getName(), login.getPassword(), credentials.get())) {
            throw new SatellizerException(HttpStatus.UNAUTHORIZED, "Invalid login, check email and password");
        }

        return OAuthUserService.createToken(request.getClientAddress(), user.get().getName());
    }
}
