package samples.satellizer;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.ImmutableSet;
import org.jongo.marshall.jackson.oid.Id;
import org.jongo.marshall.jackson.oid.ObjectId;
import restx.security.RestxPrincipal;

import java.util.HashSet;
import java.util.Set;

/**
 * Date: 4/1/15
 * Time: 23:08
 */
public class AppUser implements RestxPrincipal {
    @Id @ObjectId
    private String id;

    private String userName;

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

    public String getUserName() {
        return userName;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public AppUser setId(final String id) {
        this.id = id;
        return this;
    }

    public AppUser setUserName(final String userName) {
        this.userName = userName;
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
