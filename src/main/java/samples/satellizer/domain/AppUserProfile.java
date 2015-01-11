package samples.satellizer.domain;

/**
 * Date: 5/1/15
 * Time: 21:01
 */
public class AppUserProfile extends AppUser {
    private boolean facebook;
    private boolean google;
    private boolean twitter;

    public boolean isFacebook() {
        return facebook;
    }

    public boolean isGoogle() {
        return google;
    }

    public boolean isTwitter() {
        return twitter;
    }

    public AppUserProfile setFacebook(final boolean facebook) {
        this.facebook = facebook;
        return this;
    }

    public AppUserProfile setGoogle(final boolean google) {
        this.google = google;
        return this;
    }

    public AppUserProfile setTwitter(final boolean twitter) {
        this.twitter = twitter;
        return this;
    }
}
