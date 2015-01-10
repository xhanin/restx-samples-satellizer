package restx.security.oauth;

import org.hibernate.validator.constraints.NotBlank;

public class OAuthPayload {
	@NotBlank
	private String clientId;

	@NotBlank
	private String redirectUri;

	@NotBlank
	private String code;

	public String getClientId() {
		return clientId;
	}

	public String getRedirectUri() {
		return redirectUri;
	}

	public String getCode() {
		return code;
	}

	public OAuthPayload setClientId(final String clientId) {
		this.clientId = clientId;
		return this;
	}

	public OAuthPayload setRedirectUri(final String redirectUri) {
		this.redirectUri = redirectUri;
		return this;
	}

	public OAuthPayload setCode(final String code) {
		this.code = code;
		return this;
	}

	@Override
	public String toString() {
		return "Payload{" +
				"clientId='" + clientId + '\'' +
				", redirectUri='" + redirectUri + '\'' +
				", code='" + code + '\'' +
				'}';
	}
}
