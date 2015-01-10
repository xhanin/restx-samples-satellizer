package restx.security.oauth;

import com.google.common.base.Optional;
import restx.config.Settings;
import restx.config.SettingsKey;

@Settings
public interface ClientSecretsSettings {
	@SettingsKey(key = "restx.oauth.secrets.token")
	String getTokenSecret();

	@SettingsKey(key = "restx.oauth.secrets.facebook")
	public Optional<String> getFacebook();

	@SettingsKey(key = "restx.oauth.secrets.google")
	public Optional<String> getGoogle();

	@SettingsKey(key = "restx.oauth.secrets.linkedin")
	public Optional<String> getLinkedin();

	@SettingsKey(key = "restx.oauth.secrets.foursquare")
	public Optional<String> getFoursquare();

	@SettingsKey(key = "restx.oauth.secrets.twitter")
	public Optional<String> getTwitter();
}
