package restx.security.oauth;

import com.google.common.base.Optional;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import restx.RestxRequest;
import restx.security.RestxPrincipal;
import restx.security.RestxSession;

public class OAuthUserService<U extends RestxPrincipal> {
	private static final Logger logger = LoggerFactory.getLogger(OAuthUserService.class);

	private static final JWSHeader JWT_HEADER = new JWSHeader(JWSAlgorithm.HS256);
	private final String tokenSecret;
	private final OAuthUserRepository<U> dao;

	public OAuthUserService(ClientSecretsSettings secrets, OAuthUserRepository<U> dao) {
		this.dao = dao;
		this.tokenSecret = secrets.getTokenSecret();
	}

	public Token processUser(RestxRequest request, ProviderUserInfo providerUserInfo) {
		logger.debug("{} authenticated with {} - {}",
				providerUserInfo.getDisplayName().or(providerUserInfo.getEmail()).or(providerUserInfo.getUserIdForProvider()),
				providerUserInfo.getProviderName(),
				providerUserInfo.getUserInfo());

		U user;

		RestxSession current = RestxSession.current();

		if (current != null && current.getPrincipal().isPresent()) {
			user = processUserToLink(current.getPrincipal().get(), providerUserInfo);

		} else {
			user = processUserToCreate(providerUserInfo);
		}

		return createToken(request.getClientAddress(), user.getName());
	}

	protected U processUserToCreate(ProviderUserInfo providerUserInfo) {
		U user;// Create a new user account or return an existing one.
		Optional<U> userFromDb = dao.findByProvider(
                providerUserInfo.getProviderName(), providerUserInfo.getUserIdForProvider());

		if (userFromDb.isPresent()) {
            user = userFromDb.get();
        } else {
            user = dao.createNewUserWithLinkedProviderAccount(providerUserInfo);
        }
		return user;
	}

	protected U processUserToLink(RestxPrincipal principal, ProviderUserInfo providerUserInfo) {
		U user;// If user is already signed in then link accounts.
		Optional<U> userFromDb = dao.findByProvider(
                providerUserInfo.getProviderName(), providerUserInfo.getUserIdForProvider());

		if (userFromDb.isPresent()) {
            // the user is already linked to that provider
            user = userFromDb.get();
        } else {
            user = castPrincipal(principal);

            dao.linkProviderAccount(user, providerUserInfo);
        }
		return user;
	}

	public Token createToken(String host, String subject) {
		JWTClaimsSet claim = new JWTClaimsSet();
		claim.setSubject(subject);
		claim.setIssuer(host);
		claim.setIssueTime(DateTime.now().toDate());
		claim.setExpirationTime(DateTime.now().plusDays(14).toDate());

		JWSSigner signer = new MACSigner(tokenSecret);
		SignedJWT jwt = new SignedJWT(JWT_HEADER, claim);

		try {
			jwt.sign(signer);
		} catch (JOSEException e) {
			throw new IllegalStateException(e);
		}

		return new Token(jwt.serialize());
	}

	@SuppressWarnings("unchecked")
	private U castPrincipal(RestxPrincipal principal) {
		return (U) principal;
	}
}
