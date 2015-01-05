package samples.satellizer;

/**
 * Date: 5/1/15
 * Time: 22:20
 */
public class AppUserSignup extends AppUser {
    private String password;

    public String getPassword() {
        return password;
    }

    public AppUserSignup setPassword(final String password) {
        this.password = password;
        return this;
    }
}
