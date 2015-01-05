package samples.satellizer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;
import org.jongo.marshall.jackson.oid.Id;
import restx.security.RestxPrincipal;

import java.util.HashSet;
import java.util.Set;

/**
 * Date: 4/1/15
 * Time: 23:08
 */
public class AppUser implements RestxPrincipal {
    @Id
    private String id;

    private String email;

    private String displayName;

    private Set<String> roles = new HashSet<>();

    @Override
    @JsonIgnore
    public ImmutableSet<String> getPrincipalRoles() {
        return ImmutableSet.copyOf(roles);
    }

    @Override
    @JsonIgnore
    public String getName() {
        return id;
    }

    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public AppUser setId(final String id) {
        this.id = id;
        return this;
    }

    public AppUser setEmail(final String email) {
        this.email = email;
        return this;
    }

    public AppUser setDisplayName(final String displayName) {
        this.displayName = displayName;
        return this;
    }

    public AppUser setRoles(final Set<String> roles) {
        this.roles = roles;
        return this;
    }

    @Override
    public String toString() {
        return "AppUser{" +
                "id='" + id + '\'' +
                ", roles=" + roles +
                '}';
    }
}
