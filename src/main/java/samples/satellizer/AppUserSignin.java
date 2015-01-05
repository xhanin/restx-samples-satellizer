package samples.satellizer;

/**
 * Date: 5/1/15
 * Time: 22:50
 */
public class AppUserSignin {
    private String email;
    private String password;

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }

    public AppUserSignin setEmail(final String email) {
        this.email = email;
        return this;
    }

    public AppUserSignin setPassword(final String password) {
        this.password = password;
        return this;
    }

    @Override
    public String toString() {
        return "AppUserSignin{" +
                "email='" + email + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
