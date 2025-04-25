package idv.clu.gateway.iam.exception;

/**
 * @author clu
 */
public class UserAlreadyExistsException extends RuntimeException {

    private final String realmName;
    private final String username;

    public UserAlreadyExistsException(String realmName, String username) {
        super(String.format("User '%s' already exists in realm: %s", username, realmName));
        this.realmName = realmName;
        this.username = username;
    }

    public String getRealmName() {
        return realmName;
    }

    public String getUsername() {
        return username;
    }

}
