package restx.security.oauth;

import com.google.common.base.Optional;

import java.util.HashMap;
import java.util.Map;

/**
 * Date: 5/1/15
 * Time: 21:50
 */
public class ProviderUserInfo {
    private String providerName;
    private String userIdForProvider;

    private Optional<String> displayName = Optional.absent();
    private Optional<String> email = Optional.absent();
    private Map<String, ?> userInfo = new HashMap<>();

    public String getProviderName() {
        return providerName;
    }

    public String getUserIdForProvider() {
        return userIdForProvider;
    }

    public Optional<String> getDisplayName() {
        return displayName;
    }

    public Optional<String> getEmail() {
        return email;
    }

    public Map<String, ?> getUserInfo() {
        return userInfo;
    }

    public ProviderUserInfo setProviderName(final String providerName) {
        this.providerName = providerName;
        return this;
    }

    public ProviderUserInfo setUserIdForProvider(final String userIdForProvider) {
        this.userIdForProvider = userIdForProvider;
        return this;
    }

    public ProviderUserInfo setDisplayName(final Optional<String> displayName) {
        this.displayName = displayName;
        return this;
    }

    public ProviderUserInfo setEmail(final Optional<String> email) {
        this.email = email;
        return this;
    }

    public ProviderUserInfo setUserInfo(final Map<String, ?> userInfo) {
        this.userInfo = userInfo;
        return this;
    }

    @Override
    public String toString() {
        return "ProviderUserInfo{" +
                "providerName='" + providerName + '\'' +
                ", userIdForProvider='" + userIdForProvider + '\'' +
                ", displayName=" + displayName +
                ", email=" + email +
                ", userInfo=" + userInfo +
                '}';
    }
}
