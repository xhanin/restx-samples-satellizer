package samples.satellizer;

import com.google.common.base.Optional;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.RestxContext;
import restx.RestxFilter;
import restx.RestxHandler;
import restx.RestxHandlerMatch;
import restx.RestxRequest;
import restx.RestxRequestMatch;
import restx.RestxResponse;
import restx.StdRestxRequestMatch;
import restx.WebException;
import restx.factory.Component;
import restx.http.HttpStatus;
import restx.security.BasicPrincipalAuthenticator;
import restx.security.RestxPrincipal;
import restx.security.RestxSession;

import java.io.IOException;
import java.text.ParseException;
import java.util.Locale;

/**
 * Date: 5/1/15
 * Time: 21:13
 */
@Component(priority = -195)
public class JWTAuthenticationFilter implements RestxFilter {
    private static final Logger logger = LoggerFactory.getLogger(JWTAuthenticationFilter.class);

    private final BasicPrincipalAuthenticator authenticator;

    private RestxHandler bearerHandler = new RestxHandler() {
        @Override
        public void handle(RestxRequestMatch match, RestxRequest req, RestxResponse resp, RestxContext ctx) throws IOException {
            JWTClaimsSet claimSet;
            try {
                claimSet = (JWTClaimsSet) decodeToken(req.getHeader("Authorization").get());
            } catch (ParseException e) {
                throw new WebException(HttpStatus.BAD_REQUEST, "Invalid JWT Token - " + e.getMessage());
            }

            // ensure that the token is not expired
            if (new DateTime(claimSet.getExpirationTime()).isBefore(DateTime.now())) {
                throw new WebException(HttpStatus.UNAUTHORIZED, "JWT Token expired");
            } else {
                Optional<? extends RestxPrincipal> principal = authenticator.findByName(claimSet.getSubject());

                if (principal.isPresent()) {
                    logger.debug("JWT authenticated '{}'", principal.get().getName());

                    RestxSession.current().authenticateAs(principal.get());
                    try {
                        ctx.nextHandlerMatch().handle(req, resp, ctx);
                    } finally {
                        // we don't want to keep the principal in restx session, we want to trust the bearer only
                        RestxSession.current().clearPrincipal();
                    }
                } else {
                    throw new WebException(HttpStatus.UNAUTHORIZED, "Principal unknown");
                }
            }
        }

        public ReadOnlyJWTClaimsSet decodeToken(String authHeader) throws ParseException {
            return SignedJWT.parse(getSerializedToken(authHeader)).getJWTClaimsSet();
        }

        public String getSerializedToken(String authHeader) {
            return authHeader.split(" ")[1];
        }
    };

    public JWTAuthenticationFilter(BasicPrincipalAuthenticator authenticator) {
        this.authenticator = authenticator;
    }

    @Override
    public Optional<RestxHandlerMatch> match(RestxRequest req) {
        Optional<String> authorization = req.getHeader("Authorization");
        if (authorization.isPresent()) {
            if (authorization.get().toLowerCase(Locale.ENGLISH).startsWith("bearer ")) {
                return Optional.of(new RestxHandlerMatch(
                        new StdRestxRequestMatch("*", req.getRestxPath()),
                        bearerHandler));
            }
        }
        return Optional.absent();
    }


}
