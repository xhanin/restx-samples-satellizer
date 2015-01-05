package samples.satellizer;

import com.google.common.base.Optional;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import org.joda.time.DateTime;
import restx.RestxRequest;
import restx.security.RestxPrincipal;
import restx.security.RestxSession;

public class AuthUtils<U extends RestxPrincipal> {
	private static final JWSHeader JWT_HEADER = new JWSHeader(JWSAlgorithm.HS256);
	private final String tokenSecret;
	private final OAuthUserRepository<U> dao;

	public AuthUtils(ClientSecretsSettings secrets, OAuthUserRepository<U> dao) {
		this.dao = dao;
		this.tokenSecret = secrets.getTokenSecret();
	}

	public Token processUser(RestxRequest request, ProviderUserInfo providerUserInfo) {
		U user;

		RestxSession current = RestxSession.current();

		if (current != null && current.getPrincipal().isPresent()) {
			// Step 3a. If user is already signed in then link accounts.
			Optional<U> userFromDb = dao.findByProvider(
					providerUserInfo.getProviderName(), providerUserInfo.getUserIdForProvider());

			if (userFromDb.isPresent()) {
				// the user is already linked to that provider
				user = userFromDb.get();
			} else {
				user = getUserFromSession(current);

				dao.linkProviderAccount(user, providerUserInfo);
			}
		} else {
			// Step 3b. Create a new user account or return an existing one.
			Optional<U> userFromDb = dao.findByProvider(
					providerUserInfo.getProviderName(), providerUserInfo.getUserIdForProvider());

			if (userFromDb.isPresent()) {
				user = userFromDb.get();
			} else {
				user = dao.createNewUserWithLinkedProviderAccount(providerUserInfo);
			}
		}

		return createToken(request.getClientAddress(), user.getName());
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
	private U getUserFromSession(RestxSession current) {
		return (U) current.getPrincipal().get();
	}
}
