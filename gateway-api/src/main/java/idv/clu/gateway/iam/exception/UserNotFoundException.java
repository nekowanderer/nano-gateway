package idv.clu.gateway.iam.exception;

/**
 * @author clu
 */
public class UserNotFoundException extends RuntimeException {

    private final String realmName;
    private final String userId;
    private final String username;

    public UserNotFoundException(String realmName, String userId, String username) {
        super(String.format("User not found in realm: %s, userId: %s, username: %s", realmName, userId, username));
        this.realmName = realmName;
        this.username = username;
        this.userId = userId;
    }

    public String getRealmName() {
        return realmName;
    }

    public String getUsername() {
        return username;
    }

    public String getUserId() {
        return userId;
    }

}
