package idv.clu.gateway.iam.exception;

/**
 * @author clu
 */
public class UserNotFoundException extends RuntimeException {

    private final String realm;

    public UserNotFoundException(String realm) {
        super(String.format("User not found in realm: %s", realm));
        this.realm = realm;
    }

    public String getRealm() {
        return realm;
    }

}
