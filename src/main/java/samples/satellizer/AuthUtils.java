package samples.satellizer;

import java.text.ParseException;

import org.joda.time.DateTime;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.JWSSigner;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.ReadOnlyJWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import restx.factory.Component;

@Component
public final class AuthUtils {
	public static final String AUTH_HEADER_KEY = "Authorization";

	private static final JWSHeader JWT_HEADER = new JWSHeader(JWSAlgorithm.HS256);
	private final String tokenSecret;

	public AuthUtils(ClientSecretsSettings secrets) {
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
}
