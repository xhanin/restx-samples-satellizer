package samples.satellizer;

import java.text.ParseException;
import java.util.Map;

import com.google.common.base.Optional;
import com.google.common.base.Strings;
import org.joda.time.DateTime;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import restx.RestxRequest;
import restx.WebException;
import restx.http.HttpStatus;
import restx.security.RestxPrincipal;

public class AuthUtils<U extends RestxPrincipal> {
	public static final String AUTH_HEADER_KEY = "Authorization";

	private static final JWSHeader JWT_HEADER = new JWSHeader(JWSAlgorithm.HS256);
	private final String tokenSecret;
	private final OAuthUserRepository<U> dao;

	public AuthUtils(ClientSecretsSettings secrets, OAuthUserRepository<U> dao) {
		this.dao = dao;
		this.tokenSecret = secrets.getTokenSecret();
	}

	public String getSubject(String authHeader) {
		return decodeToken(authHeader).getSubject();
	}
	
	public ReadOnlyJWTClaimsSet decodeToken(String authHeader) {
		try {
			return SignedJWT.parse(getSerializedToken(authHeader)).getJWTClaimsSet();
		} catch (ParseException e) {
			throw new IllegalArgumentException(e);
		}
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
	
	public static String getSerializedToken(String authHeader) {
		return authHeader.split(" ")[1];
	}

	public Token processUser(RestxRequest request, String providerName, String userIdForProvider,
							 String userName, Map<String, Object> userInfo) {
		U user;

		String authHeader = request.getHeader(AuthUtils.AUTH_HEADER_KEY).or("");
		if (!Strings.isNullOrEmpty(authHeader)) {
			// Step 3a. If user is already signed in then link accounts.
			if (dao.findByProvider(providerName, userIdForProvider).isPresent()) {
				throw new WebException(HttpStatus.CONFLICT,
						String.format("There is already a %s account that belongs to you", providerName));
			}

			Optional<U> userFromDb = dao.findUserByName(getSubject(authHeader));
			if (!userFromDb.isPresent()) {
				throw new WebException(HttpStatus.UNAUTHORIZED, "Current user unknown");
			}

			user = userFromDb.get();

			dao.linkProviderAccount(user, providerName, userIdForProvider, userName, userInfo);
		} else {
			// Step 3b. Create a new user account or return an existing one.
			Optional<U> userFromDb = dao.findByProvider(providerName, userIdForProvider);

			if (userFromDb.isPresent()) {
				user = userFromDb.get();
			} else {
				user = dao.createNewUserWithLinkedProviderAccount(providerName, userIdForProvider, userName, userInfo);
			}
		}

		return createToken(request.getClientAddress(), user.getName());
	}
}
